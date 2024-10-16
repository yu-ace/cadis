package cat.redis.cadis.server.serverCommand.command;

import cat.redis.cadis.server.serverCommand.CommandResult;
import cat.redis.cadis.server.serverCommand.ServerCommand;
import cat.redis.cadis.server.storage.MemoryStorage;
import cat.redis.cadis.server.storage.models.Record;

public class ExistsCommand implements ServerCommand {
    @Override
    public String getName() {
        return "exists";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) {
        CommandResult commandResult = new CommandResult();

        Record record = storage.get(key);
        if(record.getType() == null){
            commandResult.setData("0".getBytes());
        }
        commandResult.setData("1".getBytes());

        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("exists");
        commandResult.setType(-1);
        commandResult.setList(false);
        return commandResult;
    }
}