package serverCommand;

import cat.redis.cadis.server.storage.MemoryStorage;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    Map<String,Class<? extends ServerCommand>> map;

    public CommandFactory(){
        map = new HashMap<>();
        map.put("set",SetCommand.class);
        map.put("get",GetCommand.class);
        map.put("setNX",SetNXCommand.class);
        map.put("exists",ExistsCommand.class);
        map.put("delete",DeleteCommand.class);
        map.put("list",ListCommand.class);
        map.put("stat",StatCommand.class);
        map.put("shutDown",ShutDownCommand.class);
        map.put("incr",IncrCommand.class);
        map.put("decr",DecrCommand.class);
    }

    public CommandResult create(String cmdStr, MemoryStorage storage) throws Exception {
        String[] strings = cmdStr.split(" ");
        String name = strings.length > 0 ? strings[0] : null;
        String key = strings.length > 1 ? strings[1] : null;
        String value = strings.length > 2 ? strings[2] : null;

        Class<? extends ServerCommand> clazz = map.get(name);
        ServerCommand serverCommand = clazz.getDeclaredConstructor().newInstance();
        CommandResult execute = serverCommand.execute(name, key, value, storage);
        return execute;
    }
}
