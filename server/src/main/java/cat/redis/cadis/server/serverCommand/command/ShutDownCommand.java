package cat.redis.cadis.server.serverCommand.command;

import cat.redis.cadis.server.serverCommand.CommandResult;
import cat.redis.cadis.server.serverCommand.ServerCommand;
import cat.redis.cadis.server.storage.MemoryStorage;

public class ShutDownCommand implements ServerCommand {
    @Override
    public String getName() {
        return "shutDown";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) throws Exception{
        CommandResult commandResult = new CommandResult();

        try{
            storage.shutDown();
        }catch (Exception e){
            e.getStackTrace();
        }
        commandResult.setData("save ok".getBytes());

        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("shutDown");
        commandResult.setType(1);
        commandResult.setList(false);
        return commandResult;
    }
}
