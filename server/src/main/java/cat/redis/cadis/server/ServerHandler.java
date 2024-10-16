package cat.redis.cadis.server;

import cat.redis.cadis.server.serverCommand.ServerCommand;
import cat.redis.cadis.server.storage.MemoryStorage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import cat.redis.cadis.server.serverCommand.CommandFactory;
import cat.redis.cadis.server.serverCommand.CommandResult;

import java.nio.ByteBuffer;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    MemoryStorage memoryStorage;
    CommandFactory commandFactory;

    public ServerHandler() {
    }

    public ServerHandler(MemoryStorage memoryStorage) {
        this.memoryStorage = memoryStorage;
        commandFactory = new CommandFactory();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        processMessage(ctx,(ByteBuf) msg);
    }

    private void processMessage(ChannelHandlerContext ctx,ByteBuf msg) throws Exception{

        //获取客户端发送过来的消息
        String message = msg.toString(CharsetUtil.UTF_8);

        System.out.println("收到客户端的消息："+message);

        String[] strings = message.split(" ");
        String name = strings.length > 0 ? strings[0] : null;
        String key = strings.length > 1 ? strings[1] : null;
        String value = strings.length > 2 ? strings[2] : null;

        ServerCommand serverCommand = commandFactory.create(name);
        String result;
        result = getResult(name, key, value, serverCommand);

        ctx.writeAndFlush(Unpooled.copiedBuffer(result, CharsetUtil.UTF_8));
    }

    private String getResult(String name, String key, String value, ServerCommand serverCommand) {
        String result;
        if(serverCommand == null) {
            return  "null";
        }
        CommandResult execute = serverCommand.execute(name, key, value, memoryStorage);
        if(execute.getData() != null && execute.getType() != 0){
            result = new String(execute.getData(), CharsetUtil.UTF_8);
        }else if("set".equals(name) && execute.getType() == 0){
            result = String.valueOf(ByteBuffer.wrap(execute.getData()).getInt());
        }else if("get".equals(name) && execute.getType() == 0){
            result = new String(execute.getData(), CharsetUtil.UTF_8);
        }else {
            result = "null";
        }
        return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常，关闭通道
        ctx.close();
    }
}
