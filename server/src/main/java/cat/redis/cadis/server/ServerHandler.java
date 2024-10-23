package cat.redis.cadis.server;

import cat.redis.cadis.server.serverCommand.ServerCommand;
import cat.redis.cadis.server.storage.MemoryStorage;
import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONUtil;
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

    public ServerHandler(MemoryStorage memoryStorage,CommandFactory commandFactory) {
        this.memoryStorage = memoryStorage;
        this.commandFactory = commandFactory;
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
        String s;
        if(serverCommand != null){
            CommandResult result = serverCommand.execute(name, key, value, memoryStorage);
            s = JSONUtil.toJsonStr(result);
        }else {
            s = "null";
        }

        //内容交给客户端解析
        ctx.writeAndFlush(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常，关闭通道
        ctx.close();
    }


    public static void main(String[] args) {
        ByteBuffer a = ByteBuffer.allocate(4);
        a.putInt(912);
        byte[] ab = a.array();
        String encode = Base64.encode(ab);
        byte[] decode = Base64.decode(encode);
        System.out.println(1);
    }
}
