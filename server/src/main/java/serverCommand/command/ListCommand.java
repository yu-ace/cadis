package serverCommand.command;

import cat.redis.cadis.server.storage.MemoryStorage;
import io.netty.util.CharsetUtil;
import serverCommand.CommandResult;
import serverCommand.ServerCommand;

import java.util.Set;

public class ListCommand extends ServerCommand {
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
            byte[] result = stringBuilder.toString().getBytes(CharsetUtil.UTF_8);
            commandResult.setData(result);
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