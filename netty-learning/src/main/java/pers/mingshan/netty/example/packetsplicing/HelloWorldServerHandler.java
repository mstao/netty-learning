package pers.mingshan.netty.example.packetsplicing;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HelloWorldServerHandler extends ChannelInboundHandlerAdapter{
    private int counter;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String)msg;
        System.out.println("server receive order : " + body + ";the counter is: " + ++counter);
        ctx.writeAndFlush(body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (this == ctx.pipeline().last()) {
            System.out.println("客户端已经离开");
        }
        ctx.close();
    }
}
