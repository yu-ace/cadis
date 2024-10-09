package serverCommand;

import cat.redis.cadis.server.service.models.Command;
import cat.redis.cadis.server.storage.MemoryStorage;
import cat.redis.cadis.server.storage.models.Record;

import java.nio.ByteBuffer;

public class SetNXCommand extends ServerCommand{
    @Override
    public String getName() {
        return "setNX";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) {
        CommandResult commandResult = new CommandResult();

        if(key != null){
                Record record = storage.get(key);
                if(record.getType() != null){
                    commandResult.setData("0".getBytes());
                }
                byte[] newValue = ByteBuffer.allocate(4).putInt(1).array();
                storage.set(storage.buffer,storage.map,key, newValue,"Integer");
            commandResult.setData("1".getBytes());
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
