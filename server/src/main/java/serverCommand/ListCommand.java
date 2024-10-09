package serverCommand;

import cat.redis.cadis.server.storage.MemoryStorage;

import java.util.Set;

public class ListCommand extends ServerCommand{
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) {
        CommandResult commandResult = new CommandResult();

        StringBuilder stringBuilder = new StringBuilder();
        Set<String> keys = storage.keyList();
        if(keys.size() != 0){
            for(String k:keys){
                stringBuilder.append(k).append(" ");
            }
            commandResult.setData(commandResult.getData());
        }else {
            commandResult.setData(null);
        }

        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("stat");
        commandResult.setType(1);
        commandResult.setList(true);
        return commandResult;

    }
}
