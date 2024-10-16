package cat.redis.cadis.server.storage;

import cat.redis.cadis.server.config.ServerConfig;
import cat.redis.cadis.server.storage.models.Index;
import cat.redis.cadis.server.storage.models.Record;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MemoryStorage {
    public Map<String, Index> map;
    public ByteBuffer buffer;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
    String dataPath;
    String mapPath;
    Integer totalMemory;
    public MemoryStorage(){}

    public MemoryStorage(ServerConfig serverConfig) throws Exception {
        dataPath = serverConfig.getDataPath();
        mapPath = serverConfig.getMapPath();
        totalMemory = serverConfig.getTotalMemory();
        init();
    }

    public void init() throws Exception{
        initMap();
        initData();
    }

    public void initMap() {
        File file = new File(mapPath);
        map = new HashMap<>();
        if(file.exists()){
            try{
                FileInputStream fileInputStream = new FileInputStream(mapPath);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                map = (Map<String, Index>) objectInputStream.readObject();
            }catch (Exception e){
                e.getStackTrace();
            }
        }
    }

    public void initData() throws Exception {
        File file = new File(dataPath);
        if(file.exists()){
            try(RandomAccessFile read = new RandomAccessFile(dataPath, "rw")) {
                long length = read.length();
                buffer = ByteBuffer.allocate((int) length);
                FileChannel channel = read.getChannel();
                channel.read(buffer);
                buffer.flip();
            }
        }else{
            buffer = ByteBuffer.allocate(totalMemory);
        }
    }

    public void shutDown() throws Exception{
        writeLock.lock();
        try{
            saveData();
            saveMap();
        }finally {
            writeLock.unlock();
        }
    }

    public void saveMap() throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(mapPath);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(map);
    }

    public void saveData() throws Exception{
        RandomAccessFile file = new RandomAccessFile(dataPath,"rw");
        FileChannel channel = file.getChannel();
        buffer.flip();
        buffer.limit(buffer.capacity());
        channel.write(buffer);
        file.close();
    }

    public Record get(String key) {
        readLock.lock();
        try{
            Record record;
            if(map.containsKey(key)){
                buffer.limit(totalMemory);
                Index index = map.get(key);
                buffer.position(index.getPosition());
                buffer.limit(index.getPosition() + index.getLength());
                ByteBuffer slice = buffer.slice();
                byte[] bytes = new byte[slice.remaining()];
                slice.get(bytes);
                record = new Record(key,bytes,map.get(key).getType());
            }else {
                record = new Record(key,null,null);
            }
            return record;
        }finally {
            readLock.unlock();
        }
    }


    public void set(ByteBuffer byteBuffer, Map<String, Index> map, String key, byte[] value,String type) {
        writeLock.lock();
        try{
            int valurLength = value.length;
            byteBuffer.position(0);
            int size = byteBuffer.getInt();
            byteBuffer.position(0);
            byteBuffer.putInt(size + 1);
            int position = 4 + 8 * size;
            int startPosition;
            if(size == 0){
                startPosition = totalMemory - valurLength;
            }else {
                byteBuffer.position(position - 8);
                int lastPosition = byteBuffer.getInt();
                startPosition = lastPosition - valurLength;
            }
            byteBuffer.position(position);
            byteBuffer.putInt(startPosition);
            byteBuffer.putInt(valurLength);
            byteBuffer.position(startPosition);
            byteBuffer.put(value);
            Index index = new Index(startPosition, valurLength, type);

            map.put(key,index);
        }finally {
            writeLock.unlock();
        }
    }


    public String increment(String key){
        writeLock.lock();
        try{
            Record record = get(key);
            if("Integer".equals(record.getType())){
                ByteBuffer byteBuffer = ByteBuffer.wrap(record.getValue());
                int value = byteBuffer.getInt();
                value++;
                byte[] newValue = ByteBuffer.allocate(4).putInt(value).array();
                set(buffer,map,key,newValue,"Integer");
                return Integer.toString(value);
            }else if(record.getType() == null){
                byte[] newValue = ByteBuffer.allocate(4).putInt(1).array();
                set(buffer,map,key,newValue,"Integer");
                return Integer.toString(1);
            }else {
                return "null";
            }
        }finally {
            writeLock.unlock();
        }
    }

    public String decrement(String key){
        writeLock.lock();
        try{
            Record record = get(key);
            if("Integer".equals(record.getType())){
                ByteBuffer byteBuffer = ByteBuffer.wrap(record.getValue());
                int value = byteBuffer.getInt();
                value--;
                byte[] newValue = ByteBuffer.allocate(4).putInt(value).array();
                set(buffer,map,key,newValue,"Integer");
                return Integer.toString(value);
            }else if(record.getType() == null){
                byte[] newValue = ByteBuffer.allocate(4).putInt(1).array();
                set(buffer,map,key,newValue,"Integer");
                return Integer.toString(-1);
            }else {
                return "null";
            }
        }finally {
            writeLock.unlock();
        }
    }

    public Set<String> keyList(){
        readLock.lock();
        try{
            return map.keySet();
        }finally {
            readLock.unlock();
        }
    }

    public String delete(String key){
        writeLock.lock();
        try{
            String response;
            if(!map.containsKey(key)){
                response = "key is null";
            }else {
                map.remove(key);
                response = "delete ok";
            }
            return response;
        }finally {
            writeLock.unlock();
        }
    }

    public Integer usedMemory(){
        readLock.lock();
        try{
            int usedMemory;
            buffer.position(0);
            int size = buffer.getInt();
            if(size != 0){
                int index = 4 + 8 * (size - 1);
                buffer.position(index);
                int lastStartPosition = buffer.getInt();
                int userHeadPosition = 4 + 8 * size;
                usedMemory = totalMemory - lastStartPosition + userHeadPosition;
            }else {
                usedMemory = 0;
            }
            return usedMemory;
        }finally {
            readLock.unlock();
        }
    }

    public Integer freeMemory(){
        readLock.lock();
        try{
            return totalMemory - usedMemory();
        }finally {
            readLock.unlock();
        }
    }

    public void clean() {
        writeLock.lock();
        try{
            System.out.println("clean");
            ByteBuffer byteBuffer = ByteBuffer.allocate(totalMemory);
            Set<String> strings = keyList();
            for (String s : strings) {
                set(byteBuffer, map, s, get(s).getValue(),get(s).getType());
            }
            buffer = byteBuffer;
        }finally {
            writeLock.unlock();
        }
    }

}
