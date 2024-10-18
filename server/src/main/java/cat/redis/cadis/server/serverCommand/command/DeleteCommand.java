package cat.redis.cadis.server.serverCommand.command;

import cat.redis.cadis.server.serverCommand.CommandResult;
import cat.redis.cadis.server.serverCommand.ServerCommand;
import cat.redis.cadis.server.storage.MemoryStorage;

public class DeleteCommand implements ServerCommand {
    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) throws Exception{
        String result = storage.delete(key);

        CommandResult commandResult = new CommandResult();
        commandResult.setList(false);
        commandResult.setFunctionName("delete");
        commandResult.setKey(key);
        commandResult.setData(result.getBytes());
        commandResult.setResult(true);
        commandResult.setType(-1);
        return commandResult;
    }
}
