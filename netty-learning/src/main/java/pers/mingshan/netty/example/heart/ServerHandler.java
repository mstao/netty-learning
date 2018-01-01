package pers.mingshan.netty.example.heart;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private int lossConnectTime = 0;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断一下当前的时间是不是 超时事件
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // 如果是读超时
            if (event.state() == IdleState.READER_IDLE) {
                lossConnectTime++;
                System.out.println("5 秒没有接收到客户端的信息了");
                if (lossConnectTime > 2) {
                    System.out.println("关闭这个不活跃的channel");
                    ctx.channel().close();
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       System.out.println("server channelRead..");  
       System.out.println(ctx.channel().remoteAddress() + "->Server :" + msg.toString());  
       //ctx.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
