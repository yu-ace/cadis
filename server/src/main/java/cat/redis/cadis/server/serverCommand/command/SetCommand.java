package cat.redis.cadis.server.serverCommand.command;

import cat.redis.cadis.server.serverCommand.CommandResult;
import cat.redis.cadis.server.storage.MemoryStorage;
import cat.redis.cadis.server.serverCommand.ServerCommand;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SetCommand implements ServerCommand {
    private static final Integer TYPE_INTEGER = 0;
    private static final Integer TYPE_STRING = 1;
    @Override
    public String getName() {
        return "set";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) throws Exception{
        CommandResult commandResult = new CommandResult();
        commandResult.setKey(key);
        commandResult.setList(false);
        commandResult.setType(-1);
        if(key != null && value != null){
            byte[] valueByte;
            try{
                int newValue = Integer.parseInt(value);
                valueByte = ByteBuffer.allocate(4).putInt(newValue).array();
                storage.set(key, valueByte,"Integer");
                commandResult.setType(0);
            }catch (Exception e){
                valueByte = processString(commandResult,key, value,storage);
            }
            commandResult.setData(valueByte);
        }else {
            commandResult.setData(null);
        }
        commandResult.setResult(true);
        commandResult.setFunctionName("set");
        return commandResult;
    }

    private byte[] processString(CommandResult commandResult, String key, Object value, MemoryStorage storage)
            throws Exception{
        byte[] valueByte;
        if(value instanceof String){
            valueByte = ((String) value).getBytes(StandardCharsets.UTF_8);
            storage.set(key, valueByte,"String");
            commandResult.setType(1);
        }else {
            valueByte = processList((List<?>) value);
            storage.set(key, valueByte,"List");
            commandResult.setType(2);
            commandResult.setList(true);
        }
        return valueByte;
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

}
