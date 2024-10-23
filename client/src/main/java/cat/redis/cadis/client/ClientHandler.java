package cat.redis.cadis.client;

import cat.redis.cadis.client.model.CommandResult;
import cn.hutool.json.JSONUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.util.Scanner;

public class ClientHandler extends ChannelInboundHandlerAdapter{
    private static final Integer TYPE_INTEGER = 0;
    private static final Integer TYPE_STRING = 1;
    private static final Integer TYPE_LIST = 2;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Scanner scanner = new Scanner(System.in);
        new Thread(() -> {
            while (true){
                String message = scanner.nextLine();
                ctx.writeAndFlush(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
            }
        }).start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收服务端发送过来的消息
        ByteBuf byteBuf = (ByteBuf) msg;
        String result = byteBuf.toString(CharsetUtil.UTF_8);
        if(result != null && !"null".equals(result)){
            CommandResult commandResult = JSONUtil.toBean(result, CommandResult.class);
            if("get".equals(commandResult.getName())){
                getValue(commandResult);
            }else if(commandResult.getData() != null){
                String s = new String(commandResult.getData(), CharsetUtil.UTF_8);
                System.out.println(s);
            }else {
                System.out.println("null");
            }
        }else {
            System.out.println("null");
        }
    }

    private static void getValue(CommandResult commandResult) {
        if(commandResult.getList()){
            ByteBuffer byteBuffer = ByteBuffer.wrap(commandResult.getData());
            int size = byteBuffer.getInt();
            if(TYPE_INTEGER.equals(commandResult.getType())){
                for(int i = 0;i < size;i++){
                    int intResult = byteBuffer.getInt();
                    System.out.println(intResult);
                }
            }else {
                for(int i = 0;i < size;i++){
                    int length = byteBuffer.getInt();
                    byte[] bytes = new byte[length];
                    byteBuffer.get(bytes);
                    System.out.println(new String(bytes,CharsetUtil.UTF_8));
                }
            }
        }else {
            if(TYPE_INTEGER.equals(commandResult.getType())){
                ByteBuffer byteBuffer = ByteBuffer.wrap(commandResult.getData());
                int intResult = byteBuffer.getInt();
                System.out.println(intResult);
            }else {
                String strResult = new String(commandResult.getData(), CharsetUtil.UTF_8);
                System.out.println(strResult);
            }
        }
    }
}
