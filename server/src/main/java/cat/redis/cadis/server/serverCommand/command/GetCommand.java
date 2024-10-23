package cat.redis.cadis.server.serverCommand.command;

import cat.redis.cadis.server.storage.MemoryStorage;
import cat.redis.cadis.server.storage.models.Record;
import io.netty.util.CharsetUtil;
import cat.redis.cadis.server.serverCommand.CommandResult;
import cat.redis.cadis.server.serverCommand.ServerCommand;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GetCommand implements ServerCommand {
    private static final Integer TYPE_INTEGER = 0;
    private static final Integer TYPE_STRING = 1;
    private static final Integer TYPE_LIST = 2;

    @Override
    public String getName() {
        return "get";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) throws Exception{
        CommandResult commandResult = new CommandResult();
        commandResult.setName("get");
        commandResult.setList(false);
        commandResult.setType(-1);
        Record record = storage.get(key);
        if(record.getType() != null){
            commandResult.setType(record.getType());
            commandResult.setData(record.getValue());
            if(record.getList() == 0){
                commandResult.setList(true);
            }
        }else {
            String result = "null";
            commandResult.setData(result.getBytes());
        }
        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("get");
        return commandResult;
    }

}
