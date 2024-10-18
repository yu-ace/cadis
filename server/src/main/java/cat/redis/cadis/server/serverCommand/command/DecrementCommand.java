package cat.redis.cadis.server.serverCommand.command;

import cat.redis.cadis.server.serverCommand.CommandResult;
import cat.redis.cadis.server.serverCommand.ServerCommand;
import cat.redis.cadis.server.storage.MemoryStorage;

public class DecrementCommand implements ServerCommand {
    @Override
    public String getName() {
        return "decr";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) throws Exception{
        CommandResult commandResult = new CommandResult();

        String result = storage.decrement(key);

        commandResult.setData(result.getBytes());
        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("decr");
        commandResult.setType(-1);
        commandResult.setList(false);
        return commandResult;
    }
}
