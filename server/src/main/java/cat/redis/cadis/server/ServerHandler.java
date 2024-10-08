package cat.redis.cadis.server;

import cat.redis.cadis.server.service.CommandService;
import cat.redis.cadis.server.service.models.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    CommandService commandService;
    //CmdFactory factory = new CmdFactory();

    public ServerHandler() {
    }

    public ServerHandler(CommandService commandService) {
        this.commandService = commandService;
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

        String[] strings = message.split(" ");
        String name = strings.length > 0 ? strings[0] : null;
        String key = strings.length > 1 ? strings[1] : null;
        String value = strings.length > 2 ? strings[2] : null;

        Command command = new Command(name,key,value);
        result = commandService.executeCommand(command);


        ctx.writeAndFlush(Unpooled.copiedBuffer(result.toString(), CharsetUtil.UTF_8));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常，关闭通道
        ctx.close();
    }
}
