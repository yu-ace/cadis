package cat.redis.cadis.server;

import cat.redis.cadis.server.storage.MemoryStorage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import serverCommand.CommandFactory;
import serverCommand.CommandResult;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    MemoryStorage memoryStorage;
    CommandFactory commandFactory = new CommandFactory();

    public ServerHandler() {
    }

    public ServerHandler(MemoryStorage memoryStorage) {
        this.memoryStorage = memoryStorage;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        processMessage(ctx,(ByteBuf) msg);
    }

    private void processMessage(ChannelHandlerContext ctx,ByteBuf msg) throws Exception{

        //获取客户端发送过来的消息
        String message = msg.toString(CharsetUtil.UTF_8);

        System.out.println("收到客户端的消息："+message);

        CommandResult commandResult = commandFactory.create(message, memoryStorage);
        String result = new String(commandResult.getData(), CharsetUtil.UTF_8);

        ctx.writeAndFlush(Unpooled.copiedBuffer(result, CharsetUtil.UTF_8));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常，关闭通道
        ctx.close();
    }
}
