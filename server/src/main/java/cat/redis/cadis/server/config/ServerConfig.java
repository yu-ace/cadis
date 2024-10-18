package cat.redis.cadis.server.config;

import java.util.concurrent.TimeUnit;

public class ServerConfig {
    private String inetHost;
    private Integer inetPort;

    private Integer initialDelay;
    private Integer period;
    private TimeUnit timeUnit;

    private Integer totalMemory;
    private String dataPath;
    private String mapPath;
    private String pageNumberPath;

    public ServerConfig() {
    }

    public ServerConfig(String inetHost, Integer inetPort, Integer initialDelay, Integer period, TimeUnit timeUnit,
                        Integer totalMemory, String dataPath, String mapPath, String pageNumberPath) {
        this.inetHost = inetHost;
        this.inetPort = inetPort;
        this.initialDelay = initialDelay;
        this.period = period;
        this.timeUnit = timeUnit;
        this.totalMemory = totalMemory;
        this.dataPath = dataPath;
        this.mapPath = mapPath;
        this.pageNumberPath = pageNumberPath;
    }

    public String getInetHost() {
        return inetHost;
    }

    public void setInetHost(String inetHost) {
        this.inetHost = inetHost;
    }

    public Integer getInetPort() {
        return inetPort;
    }

    public void setInetPort(Integer inetPort) {
        this.inetPort = inetPort;
    }

    public Integer getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(Integer initialDelay) {
        this.initialDelay = initialDelay;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public Integer getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(Integer totalMemory) {
        this.totalMemory = totalMemory;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getMapPath() {
        return mapPath;
    }

    public void setMapPath(String mapPath) {
        this.mapPath = mapPath;
    }

    public String getPageNumberPath() {
        return pageNumberPath;
    }

    public void setPageNumberPath(String pageNumberPath) {
        this.pageNumberPath = pageNumberPath;
    }
}
