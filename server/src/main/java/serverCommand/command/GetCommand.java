package serverCommand.command;

import cat.redis.cadis.server.service.models.Command;
import cat.redis.cadis.server.storage.MemoryStorage;
import cat.redis.cadis.server.storage.models.Record;
import io.netty.util.CharsetUtil;
import serverCommand.CommandResult;
import serverCommand.ServerCommand;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GetCommand extends ServerCommand {
    private static final Integer TYPE_STRING = 1;

    @Override
    public String getName() {
        return "get";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public CommandResult execute(String name, String key, String value, MemoryStorage storage) {
        CommandResult commandResult = new CommandResult();
        commandResult.setList(false);
        String result;
        Record record = storage.get(key);
        if(record.getType() != null){
            if("List".equals(record.getType())){
                List<Object> list = getList(record);
                result = list.toString();
                commandResult.setList(true);
                commandResult.setType(2);
            }else if("String".equals(record.getType())){
                result = new String(record.getValue(), CharsetUtil.UTF_8);
                commandResult.setType(1);
            }else {
                ByteBuffer byteBuffer = ByteBuffer.wrap(record.getValue());
                result = Integer.toString(byteBuffer.getInt());
                commandResult.setType(0);
            }
        }else {
            result = "null";
        }
        commandResult.setData(result.getBytes());
        commandResult.setKey(key);
        commandResult.setResult(true);
        commandResult.setFunctionName("get");
        return commandResult;
    }


    private List<Object> getList(Record record) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(record.getValue());
        int size = byteBuffer.getInt();
        List<Object> list = new ArrayList<>();
        for(int i = 0;i < size;i++){
            int type = byteBuffer.getInt();
            if(type == TYPE_STRING){
                int length = byteBuffer.getInt();
                byte[] bytes = new byte[length];
                byteBuffer.get(bytes);
                list.add(new String(bytes,CharsetUtil.UTF_8));
            }else {
                list.add(byteBuffer.getInt());
            }
        }
        return list;
    }
}
