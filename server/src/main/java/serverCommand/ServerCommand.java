package serverCommand;

import cat.redis.cadis.server.storage.MemoryStorage;

public abstract class ServerCommand {
    public abstract String getName();
    public abstract int getType();
    public abstract CommandResult execute(String name, String key, String value, MemoryStorage storage);
}
