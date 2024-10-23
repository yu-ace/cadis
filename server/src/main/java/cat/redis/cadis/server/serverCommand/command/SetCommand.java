package cat.redis.cadis.server.serverCommand.command;

import cat.redis.cadis.server.serverCommand.CommandResult;
import cat.redis.cadis.server.storage.MemoryStorage;
import cat.redis.cadis.server.serverCommand.ServerCommand;
import io.netty.util.CharsetUtil;

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
        commandResult.setName("set");
        commandResult.setKey(key);
        commandResult.setList(false);
        commandResult.setType(-1);
        if(key != null && value != null){
            String[] valueStr = value.split(",");
            if(valueStr.length > 1){
                processList(valueStr,key,storage,commandResult);
            }else {
                setValue(key, value, storage, commandResult);
            }
            commandResult.setData("1".getBytes());
        }else {
            commandResult.setData("0".getBytes());
        }
        commandResult.setResult(true);
        commandResult.setFunctionName("set");
        return commandResult;
    }

    private static void setValue(String key, String value, MemoryStorage storage, CommandResult commandResult)
            throws Exception {
        byte[] valueByte;
        try{
            int newValue = Integer.parseInt(value);
            valueByte = ByteBuffer.allocate(4).putInt(newValue).array();
            storage.set(key, valueByte,TYPE_INTEGER,1);
            commandResult.setType(0);
        }catch (Exception e){
            valueByte = value.getBytes(CharsetUtil.UTF_8);
            storage.set(key, valueByte,TYPE_STRING,1);
            commandResult.setType(1);
        }
    }

    private void processList(String[] value,String key, MemoryStorage storage,CommandResult commandResult) {
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeInt(value.length);
            if(isInteger(value)){
                for(String v:value){
                    byte[] byteArray = ByteBuffer.allocate(4).putInt(Integer.parseInt(v)).array();
                    dataOutputStream.write(byteArray);
                }
                commandResult.setType(TYPE_INTEGER);
            }else {
                for(String v:value) {
                    byte[] bytes = v.getBytes(StandardCharsets.UTF_8);
                    dataOutputStream.writeInt(bytes.length);
                    dataOutputStream.write(bytes);
                }
                commandResult.setType(TYPE_STRING);
            }
            byte[] valueList = byteArrayOutputStream.toByteArray();
            storage.set(key,valueList,commandResult.getType(),0);
            commandResult.setList(true);
        }catch (Exception e){
            e.getStackTrace();
        }
    }

    private boolean isInteger(String[] value){
        try{
            for(String s:value){
                Integer.parseInt(s);
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
