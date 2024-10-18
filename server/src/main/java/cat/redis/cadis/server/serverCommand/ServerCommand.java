package cat.redis.cadis.server.serverCommand;

import cat.redis.cadis.server.storage.MemoryStorage;

public interface ServerCommand {
    String getName();
    int getType();
    CommandResult execute(String name, String key, String value, MemoryStorage storage) throws Exception;
}
