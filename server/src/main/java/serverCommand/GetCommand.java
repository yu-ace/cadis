package serverCommand;

import cat.redis.cadis.server.service.models.Command;
import cat.redis.cadis.server.storage.MemoryStorage;
import cat.redis.cadis.server.storage.models.Record;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GetCommand extends ServerCommand{
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
        String result;
        Record record = storage.get(key);
        if(record.getType() != null){
            if("Integer".equals(record.getType())){
                ByteBuffer byteBuffer = ByteBuffer.wrap(record.getValue());
                result = Integer.toString(byteBuffer.getInt());
            }else if("String".equals(record.getType())){
                result = new String(record.getValue(), CharsetUtil.UTF_8);
            }else {
                List<Object> list = getList(record);
                result = list.toString();
            }
        }else {
            result = "null";
        }
        CommandResult commandResult = new CommandResult();
        commandResult.setData(result.getBytes());
        commandResult.setKey(key);
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
