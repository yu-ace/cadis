package serverCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    Map<String,Class<? extends ServerCommand>> map;

    public CommandFactory(){
        map = new HashMap<>();
        map.put("get",GetCommand.class);
    }
}
