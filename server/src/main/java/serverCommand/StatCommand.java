package serverCommand;

import cat.redis.cadis.server.storage.MemoryStorage;

public class StatCommand extends ServerCommand{
    @Override
    public String getName() {
        return "stat";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) {
        CommandResult commandResult = new CommandResult();

        Integer usedMemory = storage.usedMemory();
        int freeMemory = storage.freeMemory();
        String result = "usedMemory:" + usedMemory + "\nfreeMemory:" + freeMemory;
        commandResult.setData(commandResult.toString().getBytes());

        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("stat");
        commandResult.setType(1);
        commandResult.setList(false);
        return commandResult;
    }
}
