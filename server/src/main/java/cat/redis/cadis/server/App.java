package cat.redis.cadis.server;

import cat.redis.cadis.server.config.ConfigReader;
import cat.redis.cadis.server.config.ServerConfig;
import cat.redis.cadis.server.service.CommandService;
import cat.redis.cadis.server.service.ScheduledService;
import cat.redis.cadis.server.storage.MemoryStorage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.cli.CommandLine;

public class App {
    ScheduledService scheduledService;
    CommandService commandService;
    MemoryStorage memoryStorage;

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;

    ChannelFuture channelFuture;
    ServerBootstrap bootstrap;


    public static void main(String[] args) throws Exception{
        ConfigReader config = new ConfigReader();
        CommandLine commandLine = config.createCommandLine(args);
        String redisConfig = commandLine.getOptionValue("c");
        ServerConfig serverConfig = config.readFromFile(redisConfig);
        App app = new App();
        app.init(serverConfig);
        app.run();
    }

    public void init(ServerConfig serverConfig) throws Exception {
        scheduledService = new ScheduledService(serverConfig);
        memoryStorage = new MemoryStorage(serverConfig);
        commandService = new CommandService(memoryStorage);
        scheduledService.submitTask(memoryStorage.clean(),serverConfig.getPeriod());

        //创建两个线程组 boosGroup、workerGroup
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        //创建服务端的启动对象，设置参数
        //设置两个线程组boosGroup和workerGroup
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                //设置服务端通道实现类型
                .channel(NioServerSocketChannel.class)
                //设置线程队列得到连接个数
                .option(ChannelOption.SO_BACKLOG, 128)
                //设置保持活动连接状态
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                //使用匿名内部类的形式初始化通道对象
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //给pipeline管道设置处理器
                        socketChannel.pipeline().addLast(new ServerHandler(commandService));
                    }
                });//给workerGroup的EventLoop对应的管道设置处理器
    }

    public void run() throws Exception{
        while (true){
            try {
                //绑定端口号，启动服务端
                channelFuture = bootstrap.bind(6666).sync();
                //对关闭通道进行监听
                channelFuture.channel().closeFuture().sync();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
    }
}
