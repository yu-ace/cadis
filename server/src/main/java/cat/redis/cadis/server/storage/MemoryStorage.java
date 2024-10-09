package cat.redis.cadis.server.storage;

import cat.redis.cadis.server.config.ServerConfig;
import cat.redis.cadis.server.storage.models.Index;
import cat.redis.cadis.server.storage.models.Record;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class MemoryStorage {
    public Map<String, Index> map;
    public ByteBuffer buffer;
    private static final Integer TYPE_INTEGER = 0;
    private static final Integer TYPE_STRING = 1;

    String dataPath;
    String mapPath;
    Integer totalMemory;
    public MemoryStorage(){}

    public MemoryStorage(ServerConfig serverConfig) throws Exception {
        dataPath = serverConfig.getDataPath();
        mapPath = serverConfig.getMapPath();
        totalMemory = serverConfig.getTotalMemory();
        initMap();
        init();
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

    public void init() throws Exception {
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
            buffer = ByteBuffer.allocate(1024*1024);
        }
    }

    public void saveMap() throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(mapPath);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(map);
    }

    public void shutDown() throws Exception{
        RandomAccessFile file = new RandomAccessFile(dataPath,"rw");
        FileChannel channel = file.getChannel();
        buffer.flip();
        buffer.limit(buffer.capacity());
        channel.write(buffer);
        file.close();
    }

    public Record get(String key) {
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
    }


    public void set(ByteBuffer byteBuffer, Map<String, Index> map, String key, byte[] value,String type) {
        Index index = getIndex(byteBuffer,value, type);
        map.put(key,index);
    }

    public void set(ByteBuffer byteBuffer, Map<String, Index> map, String key, Record record) {
        Index index = getIndex(byteBuffer,record.getValue(), record.getType());
        map.put(key,index);
    }

    private Index getIndex(ByteBuffer buffer, byte[] valueByte, String type) {
        int valurLength = valueByte.length;
        buffer.position(0);
        int size = buffer.getInt();
        buffer.position(0);
        buffer.putInt(size + 1);
        int position = 4 + 8 * size;
        int startPosition;
        if(size == 0){
            startPosition = 1024*1024 - valurLength;
        }else {
            buffer.position(position - 8);
            int lastPosition = buffer.getInt();
            startPosition = lastPosition - valurLength;
        }
        buffer.position(position);
        buffer.putInt(startPosition);
        buffer.putInt(valurLength);
        buffer.position(startPosition);
        buffer.put(valueByte);

        return new Index(startPosition, valurLength, type);
    }


    public String incr(String key){
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
    }

    public String decr(String key){
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
    }

    public Set<String> keyList(){
        return map.keySet();
    }

    public String delete(String key){
        String response;
        if(!map.containsKey(key)){
            response = "key is null";
        }else {
            map.remove(key);
            response = "delete ok";
        }
        return response;
    }

    public Integer usedMemory(){
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
    }

    public Integer freeMemory(){
        return totalMemory - usedMemory();
    }

    public Runnable clean() {
        return new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    try{
                        System.out.println("clean");
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
                        Set<String> strings = keyList();
                        for(String s:strings){
                            set(byteBuffer,map,s,get(s));
                        }
                        buffer = byteBuffer;
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
            }
        };
    }

}
