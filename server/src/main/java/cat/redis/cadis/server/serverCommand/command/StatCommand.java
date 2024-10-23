package cat.redis.cadis.server.serverCommand.command;

import cat.redis.cadis.server.serverCommand.CommandResult;
import cat.redis.cadis.server.serverCommand.ServerCommand;
import cat.redis.cadis.server.storage.MemoryStorage;

public class StatCommand implements ServerCommand {
    @Override
    public String getName() {
        return "stat";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) throws Exception{
        CommandResult commandResult = new CommandResult();
        commandResult.setName("stat");

        Integer usedMemory = storage.usedMemory();
        int freeMemory = storage.freeMemory();
        String result = "usedMemory:" + usedMemory + "\nfreeMemory:" + freeMemory;
        commandResult.setData(result.getBytes());

        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("stat");
        commandResult.setType(1);
        commandResult.setList(false);
        return commandResult;
    }
}
