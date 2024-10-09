package serverCommand;

import cat.redis.cadis.server.storage.MemoryStorage;
import cat.redis.cadis.server.storage.models.Record;

public class IncrCommand extends ServerCommand{
    @Override
    public String getName() {
        return "incr";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) {
        CommandResult commandResult = new CommandResult();

        String result = storage.incr(key);

        commandResult.setData(result.getBytes());
        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("incr");
        commandResult.setType(1);
        commandResult.setList(false);
        return commandResult;
    }
}
