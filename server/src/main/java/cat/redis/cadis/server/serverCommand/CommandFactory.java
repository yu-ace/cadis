package cat.redis.cadis.server.serverCommand;

import cat.redis.cadis.server.serverCommand.command.*;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    Map<String,Class<? extends ServerCommand>> map;

    public CommandFactory(){
        map = new HashMap<>();
        map.put("set", SetCommand.class);
        map.put("get", GetCommand.class);
        map.put("setNX", SetNXCommand.class);
        map.put("exists",ExistsCommand.class);
        map.put("delete",DeleteCommand.class);
        map.put("list", ListCommand.class);
        map.put("stat", StatCommand.class);
        map.put("shutDown", ShutDownCommand.class);
        map.put("incr", IncrementCommand.class);
        map.put("decr", DecrementCommand.class);
    }

    public ServerCommand create(String name) throws Exception {
        Class<? extends ServerCommand> clazz = map.get(name);
        if(clazz == null){
            return null;
        }
        return clazz.getDeclaredConstructor().newInstance();
    }
}

