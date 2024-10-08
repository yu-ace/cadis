package cat.redis.cadis.server.config;

import cn.hutool.core.io.FileUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConfigReader {

    public CommandLine createCommandLine(String... args) throws Exception {
        Options options = new Options();
        options.addOption("h","help",false,"print help");
        options.addOption("c","configPath",true,"the path of config");
        org.apache.commons.cli.CommandLineParser parser = new DefaultParser();
        try{
            return parser.parse(options, args);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public ServerConfig readFromFile(String configPath) throws Exception {
        File[] configFiles = FileUtil.ls(configPath);
        File file = configFiles[0];
        InputStream inputStream = new FileInputStream(file);
        Yaml yaml = new Yaml();
        Map<String,Object> setting = yaml.load(inputStream);

        Iterator<String> iterator = setting.keySet().iterator();
        String NettyKey = iterator.next();
        Map<String,Object> nettyConfig = (Map<String,Object>) setting.get(NettyKey);
        String inetHost = (String) nettyConfig.get("inetHost");
        Integer inetPort = (Integer) nettyConfig.get("inetPort");
        
        String timerKey = iterator.next();
        Map<String,Object> timerConfig = (Map<String,Object>) setting.get(timerKey);
        Integer initialDelay = (Integer) timerConfig.get("initialDelay");
        Integer period = (Integer) timerConfig.get("period");
        String unit = (String) timerConfig.get("unit");
        TimeUnit timeUnit = TimeUnit.valueOf(unit);

        String configKey = iterator.next();
        Map<String,Object> config = (Map<String, Object>) setting.get(configKey);
        Integer totalMemory = (Integer) config.get("totalMemory");
        String dataPath = (String) config.get("dataPath");
        String mapPath = (String) config.get("mapPath");
        return new ServerConfig(inetHost,inetPort,initialDelay,period,timeUnit,totalMemory,dataPath,mapPath);
    }


}
