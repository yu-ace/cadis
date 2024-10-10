package serverCommand.command;

import cat.redis.cadis.server.storage.MemoryStorage;
import serverCommand.CommandResult;
import serverCommand.ServerCommand;

public class DecrCommand extends ServerCommand {
    @Override
    public String getName() {
        return "decr";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) {
        CommandResult commandResult = new CommandResult();

        String result = storage.decr(key);

        commandResult.setData(result.getBytes());
        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("decr");
        commandResult.setType(-1);
        commandResult.setList(false);
        return commandResult;
    }
}
