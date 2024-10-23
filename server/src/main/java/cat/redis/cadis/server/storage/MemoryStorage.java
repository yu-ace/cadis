package cat.redis.cadis.server.storage;

import cat.redis.cadis.server.config.ServerConfig;
import cat.redis.cadis.server.storage.models.Index;
import cat.redis.cadis.server.storage.models.Record;
import cn.hutool.core.io.file.FileWriter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MemoryStorage {
    public Map<String, Index> map;
    public Map<Integer, ByteBuffer> buffer;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
    private static final Integer TYPE_INTEGER = 0;
    private static final Integer TYPE_STRING = 1;
    String dataPath;
    String mapPath;
    String pageNumberPath;
    Integer totalMemory;
    public MemoryStorage(){}

    public MemoryStorage(ServerConfig serverConfig) throws Exception {
        dataPath = serverConfig.getDataPath();
        mapPath = serverConfig.getMapPath();
        pageNumberPath = serverConfig.getPageNumberPath();
        totalMemory = serverConfig.getTotalMemory();
        init();
    }

    public void init() throws Exception{
        initMap();
        initData();
    }

    public void initMap() {
        File file = new File(mapPath);
        map = new ConcurrentHashMap<>();
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
        Integer pageNumber = getPageNumber();
        buffer = new ConcurrentHashMap<>();
        if(file.exists()){
            for(int i = 0;i <= pageNumber;i++){
                File file1 = Paths.get(dataPath, "data_" + i + ".bin").toFile();
                try(RandomAccessFile read = new RandomAccessFile(file1, "rw")){
                    long length = read.length();
                    ByteBuffer byteBuffer = ByteBuffer.allocate((int) length);
                    FileChannel channel = read.getChannel();
                    channel.read(byteBuffer);
                    byteBuffer.flip();
                    buffer.put(i,byteBuffer);
                } catch (Exception e){
                    e.getStackTrace();
                }
            }
        }else{
            ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
            buffer.put(pageNumber,byteBuffer);
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
        File directoryFile = new File(dataPath);
        if(!directoryFile.exists()){
            directoryFile.mkdir();
        }
        Integer pageNumber = getPageNumber();
        for(int i = 0;i <= pageNumber;i++){
            String path = Paths.get(dataPath + "\\data_" + i + ".bin").toString();
            RandomAccessFile file = new RandomAccessFile(path,"rw");
            FileChannel channel = file.getChannel();
            ByteBuffer byteBuffer = buffer.get(i);
            byteBuffer.flip();
            byteBuffer.limit(byteBuffer.capacity());
            channel.write(byteBuffer);
            file.close();
        }
    }

    public Record get(String key) {
        readLock.lock();
        try{
            Record record;
            if(map.containsKey(key)){
                Index index = map.get(key);
                Integer pageNumber = index.getPageNumber();
                ByteBuffer byteBuffer = buffer.get(pageNumber);
                byteBuffer.limit(8192);
                byteBuffer.position(index.getPosition());
                byteBuffer.limit(index.getPosition() + index.getLength());
                ByteBuffer slice = byteBuffer.slice();
                byte[] bytes = new byte[slice.remaining()];
                slice.get(bytes);
                record = new Record(key,bytes,index.getType(),index.getList());
            }else {
                record = new Record(key,null,null,1);
            }
            return record;
        }finally {
            readLock.unlock();
        }
    }


    public void set(String key, byte[] value,Integer type,Integer list) throws Exception{
        writeLock.lock();
        try{
            Integer pageNumber = getPageNumber();
            ByteBuffer byteBuffer = buffer.get(pageNumber);
            int valueLength = value.length;
            byteBuffer.position(0);
            int size = byteBuffer.getInt();
            byteBuffer.position(0);
            byteBuffer.putInt(size + 1);
            int position = 4 + 8 * size;
            int startPosition;
            if(size == 0){
                //每页8k，8*1024=8192 字节
                startPosition = 8192 - valueLength;
            }else {
                //上一个数据的起始位置读取数据的插入点
                byteBuffer.position(position - 8);
                int lastPosition = byteBuffer.getInt();
                startPosition = lastPosition - valueLength;
                int remain = 8192 - 4 - size*8 - (8192-lastPosition);
                if(remain < valueLength){
                    pageNumber++;
                    savePageNumber(pageNumber);
                    byteBuffer = ByteBuffer.allocate(8192);
                    position = 4;
                    byteBuffer.putInt(1);
                    startPosition = 8192 - valueLength;
                }
            }
            byteBuffer.position(position);
            byteBuffer.putInt(startPosition);
            byteBuffer.putInt(valueLength);
            byteBuffer.position(startPosition);
            byteBuffer.put(value);
            Index index = new Index(startPosition, valueLength, type,list,pageNumber);

            buffer.put(pageNumber,byteBuffer);
            map.put(key,index);
        }finally {
            shutDown();
            writeLock.unlock();
        }
    }

    private Integer getPageNumber() throws Exception{
        File file = new File(pageNumberPath);
        int pageNumber = 0;
        if(!file.exists()){
            file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            bufferedWriter.write("0\n");
            bufferedWriter.close();
        }else {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s = bufferedReader.readLine();
            pageNumber = Integer.parseInt(s);
        }
        return pageNumber;
    }

    private void savePageNumber(Integer pageNumber) throws Exception{
        File file = Paths.get(pageNumberPath).toFile();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(pageNumber+"\n");
    }
    public String increment(String key) throws Exception{
        writeLock.lock();
        try{
            Record record = get(key);
            if(TYPE_INTEGER.equals(record.getType()) && record.getList() == 1){
                ByteBuffer newBuffer = ByteBuffer.wrap(record.getValue());
                int value = newBuffer.getInt();
                value++;
                byte[] newValue = ByteBuffer.allocate(4).putInt(value).array();
                set(key,newValue,TYPE_INTEGER,1);
                return Integer.toString(value);
            }else if(record.getType() == null){
                byte[] newValue = ByteBuffer.allocate(4).putInt(1).array();
                set(key,newValue,TYPE_INTEGER,1);
                return Integer.toString(1);
            }else {
                return "null";
            }
        }finally {
            writeLock.unlock();
        }
    }

    public String decrement(String key) throws Exception{
        writeLock.lock();
        try{
            Record record = get(key);
            if(TYPE_INTEGER.equals(record.getType()) && record.getList() == 1){
                ByteBuffer byteBuffer = ByteBuffer.wrap(record.getValue());
                int value = byteBuffer.getInt();
                value--;
                byte[] newValue = ByteBuffer.allocate(4).putInt(value).array();
                set(key,newValue,TYPE_INTEGER,1);
                return Integer.toString(value);
            }else if(record.getType() == null){
                byte[] newValue = ByteBuffer.allocate(4).putInt(-1).array();
                set(key,newValue,TYPE_INTEGER,1);
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

    public Integer usedMemory() throws Exception {
        readLock.lock();
        try{
            int usedMemory = 0;
            int currentPageUsedMemory = 0;
            Integer pageNumber = getPageNumber();
            for(int i = 0 ;i <= pageNumber;i++){
                ByteBuffer byteBuffer = buffer.get(i);
                byteBuffer.position(0);
                int size = byteBuffer.getInt();
                if(size != 0){
                    int index = 4 + 8 * (size - 1);
                    byteBuffer.position(index);
                    int lastStartPosition = byteBuffer.getInt();
                    int userHeadPosition = 4 + 8 * size;
                    currentPageUsedMemory = 8192 - lastStartPosition + userHeadPosition;
                }
                usedMemory = usedMemory + currentPageUsedMemory;
            }
            return usedMemory;
        }finally {
            readLock.unlock();
        }
    }

    public Integer freeMemory() throws Exception{
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
            Map<Integer, ByteBuffer> mapBuffer = new ConcurrentHashMap<>();
            mapBuffer.put(0,ByteBuffer.allocate(8192));
            Set<String> strings = keyList();
            List<Record> recordList = new ArrayList<>();
            for (String s : strings) {
                recordList.add(get(s));
            }
            buffer = mapBuffer;
            for (Record record : recordList) {
                set(record.getKey(), record.getValue(),record.getType(),record.getList());
            }
        }catch (Exception e){
            e.getStackTrace();
        }finally {
            writeLock.unlock();
        }
    }

}
