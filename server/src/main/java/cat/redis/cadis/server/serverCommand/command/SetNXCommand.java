package cat.redis.cadis.server.serverCommand.command;

import cat.redis.cadis.server.serverCommand.CommandResult;
import cat.redis.cadis.server.serverCommand.ServerCommand;
import cat.redis.cadis.server.storage.MemoryStorage;
import cat.redis.cadis.server.storage.models.Record;

import java.nio.ByteBuffer;

public class SetNXCommand implements ServerCommand {
    private static final Integer TYPE_INTEGER = 0;
    @Override
    public String getName() {
        return "setNX";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) throws Exception{
        CommandResult commandResult = new CommandResult();
        commandResult.setName("setNX");

        if(key != null){
            Record record = storage.get(key);
            if(record.getType() != null){
                commandResult.setData("0".getBytes());
            }else {
                byte[] newValue = ByteBuffer.allocate(4).putInt(1).array();
                storage.set(key, newValue,TYPE_INTEGER,1);
                commandResult.setData("1".getBytes());
            }
        }else {
            commandResult.setData("-1".getBytes());
        }

        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("setNX");
        commandResult.setType(1);
        commandResult.setList(false);
        return commandResult;
    }
}
