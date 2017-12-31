package pers.mingshan.netty.example.customprotocol.test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import pers.mingshan.netty.example.customprotocol.CustomProtocol;

public class ServerHandler extends ChannelInboundHandlerAdapter{
    private int counter;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 用于获取客户端发来的数据信息  
        CustomProtocol body = (CustomProtocol) msg;  
        System.out.println("the counter is: " + ++counter);
        System.out.println("Server接受的客户端的信息 :" + body.toString()
            + "\n-- 具体信息 : " + new String(body.getContent(), "UTF-8"));

        // 会写数据给客户端  
        String str = "Hi I am Server ...";  
        CustomProtocol response = new CustomProtocol(str.getBytes().length,  
                str.getBytes());  
        // 当服务端完成写操作后，关闭与客户端的连接  
        ctx.writeAndFlush(response);

        // 当有写操作时，不需要手动释放msg的引用  
        // 当只有读操作时，才需要手动释放msg的引用  
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (this == ctx.pipeline().last()) {
            System.out.println("客户端已经离开");
        }
        ctx.close();
    }
}
