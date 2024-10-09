package cat.redis.cadis.server;

import cat.redis.cadis.server.service.CommandService;
import cat.redis.cadis.server.service.models.Command;
import cat.redis.cadis.server.storage.MemoryStorage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import serverCommand.CommandFactory;
import serverCommand.CommandResult;
import serverCommand.ServerCommand;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    //CommandService commandService;
    MemoryStorage memoryStorage;
    CommandFactory commandFactory = new CommandFactory();

    public ServerHandler() {
    }

    public ServerHandler(CommandService commandService,MemoryStorage memoryStorage) {
        //this.commandService = commandService;
        this.memoryStorage = memoryStorage;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        processMessage(ctx,(ByteBuf) msg);
    }

    private void processMessage(ChannelHandlerContext ctx,ByteBuf msg) throws Exception{
        Object result;
        //获取客户端发送过来的消息
        String message = msg.toString(CharsetUtil.UTF_8);

        System.out.println("收到客户端的消息："+message);

        CommandResult commandResult = commandFactory.create(message, memoryStorage);


        ctx.writeAndFlush(Unpooled.copiedBuffer(commandResult.getFunctionName(), CharsetUtil.UTF_8));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常，关闭通道
        ctx.close();
    }
}
