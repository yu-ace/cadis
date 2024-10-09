package cat.redis.cadis.server.service;

import cat.redis.cadis.server.config.ServerConfig;
import cat.redis.cadis.server.service.models.Command;
import cat.redis.cadis.server.storage.MemoryStorage;
import cat.redis.cadis.server.storage.models.Index;
import cat.redis.cadis.server.storage.models.Record;
import io.netty.util.CharsetUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class CommandService {

    public CommandService(MemoryStorage memory) throws Exception {
        initializeCommands();
        memoryStorage = memory;
    }

    private static final Integer TYPE_INTEGER = 0;
    private static final Integer TYPE_STRING = 1;
    MemoryStorage memoryStorage;
    private final Map<String, Function<Command, String>> commandMap = new HashMap<>();

    public void initializeCommands() {
        commandMap.put("set", this::setKey);
        commandMap.put("get", this::getValue);
        commandMap.put("setNX", this::setNX);
        commandMap.put("exists",this::exists);
        commandMap.put("stat", command -> stat());
        commandMap.put("delete", this::deleteKey);
        commandMap.put("list", command -> list());
        commandMap.put("decr",this::decr);
        commandMap.put("incr",this::incr);
        commandMap.put("shutDown",command -> shutDown());
    }

    public String executeCommand(Command command) {
        String result;
        Function<Command, String> action = commandMap.get(command.getName());
        if (action != null) {
            result = action.apply(command);
        } else {
            result = "不存在的指令: " + command.getName();
        }
        return result;
    }

    public String shutDown() {
        try{
            memoryStorage.shutDown();
            memoryStorage.saveMap();
        }catch (Exception e){
            e.getStackTrace();
        }
        return "save ok";
    }

    public String exists(Command command){
        Record record = memoryStorage.get(command.getKey());
        if(record.getType() == null){
            return "0";
        }
        return "1";
    }

    public String setNX(Command command){
        if(command.getKey() != null){
            Record record = memoryStorage.get(command.getKey());
            if(record.getType() != null){
                return "0";
            }
            byte[] newValue = ByteBuffer.allocate(4).putInt(1).array();
            memoryStorage.set(memoryStorage.buffer,memoryStorage.map,command.getKey(), newValue,"Integer");
            return "1";
        }else {
            return "-1";
        }
    }

    public String incr(Command command){
        return memoryStorage.incr(command.getKey());
    }

    public String decr(Command command){
        return memoryStorage.decr(command.getKey());
    }

    public String setKey(Command command){
        if(command.getKey() != null && command.getValue() != null){
            String value = command.getValue();
            byte[] valueByte;
            try{
                int newValue = Integer.parseInt(value);
                valueByte = ByteBuffer.allocate(4).putInt(newValue).array();
                memoryStorage.set(memoryStorage.buffer,memoryStorage.map,command.getKey(), valueByte,"Integer");
            }catch (Exception e){
                processString(command, value);
            }
            return "1";
        }else {
            return "0";
        }
    }

    private void processString(Command command, Object value) {
        byte[] valueByte;
        if(value instanceof String){
            valueByte = ((String) value).getBytes(StandardCharsets.UTF_8);
            memoryStorage.set(memoryStorage.buffer,memoryStorage.map,command.getKey(), valueByte,"String");
        }else {
            valueByte = processList((List<?>) value);
            memoryStorage.set(memoryStorage.buffer,memoryStorage.map,command.getKey(), valueByte,"List");
        }
    }

    private byte[] processList(List<?> value) {
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeInt(value.size());
            for(Object v:value){
                if(v instanceof String){
                    byte[] bytes = ((String) v).getBytes(StandardCharsets.UTF_8);
                    dataOutputStream.writeInt(TYPE_STRING);
                    dataOutputStream.writeInt(bytes.length);
                    dataOutputStream.write(bytes);
                }else {
                    dataOutputStream.writeInt(TYPE_INTEGER);
                    dataOutputStream.writeInt(4);
                    dataOutputStream.write((Integer) v);
                }
            }
            return byteArrayOutputStream.toByteArray();
        }catch (Exception e){
            e.getStackTrace();
        }
        return null;
    }

    public String getValue(Command command){
        String result;
        Record record = memoryStorage.get(command.getKey());
        if(record.getType() != null){
            if("List".equals(record.getType())){
                List<Object> list = getList(record);
                result = list.toString();
            }else if("String".equals(record.getType())){
                result = new String(record.getValue(), CharsetUtil.UTF_8);
            }else {
                ByteBuffer byteBuffer = ByteBuffer.wrap(record.getValue());
                result = Integer.toString(byteBuffer.getInt());
            }
        }else {
            result = "null";
        }
        return result;
    }

    private List<Object> getList(Record record) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(record.getValue());
        int size = byteBuffer.getInt();
        List<Object> list = new ArrayList<>();
        for(int i = 0;i < size;i++){
            int type = byteBuffer.getInt();
            if(type == TYPE_STRING){
                int length = byteBuffer.getInt();
                byte[] bytes = new byte[length];
                byteBuffer.get(bytes);
                list.add(new String(bytes,CharsetUtil.UTF_8));
            }else {
                list.add(byteBuffer.getInt());
            }
        }
        return list;
    }

    public String stat(){
        Integer usedMemory = memoryStorage.usedMemory();
        int freeMemory = memoryStorage.freeMemory();
        return "usedMemory:"+ usedMemory+"\nfreeMemory:" + freeMemory;
    }

    public String deleteKey(Command command){
        return memoryStorage.delete(command.getKey());
    }

    public String list() {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> keys = memoryStorage.keyList();
        if(keys.size() != 0){
            for(String key:keys){
                stringBuilder.append(key).append(" ");
            }
            return stringBuilder.toString();
        }else {
            return "null";
        }
    }

}
