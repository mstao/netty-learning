package pers.mingshan.netty.example.heartbeat.improve;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

/**
 * 用来监测 客户端 连接服务端的状态，断开连接后进行重连
 * 对每个channel使用一个Timer或者对每个channel开启一个定时任务，定时检查该channel是否超时
 * 
 * @author mingshan
 *
 */
@Sharable
public abstract class ConnectionWatchDog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHanlderHolder {

    private final Bootstrap bootStarp;
    private final String host;
    private final int port;
    private final Timer timer;

    private volatile boolean reconnect = true;

    private int attempts;

    public ConnectionWatchDog(Bootstrap bootStarp, String host, int port,
            Timer timer, boolean reconnect) {
        super();
        this.bootStarp = bootStarp;
        this.host = host;
        this.port = port;
        this.timer = timer;
        this.reconnect = reconnect;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("当前Channel已经激活");
        attempts = 0;
        // 调用下一个ChannelInboundHandler的channelActive方法
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("当前Channel已经关闭");
        if (reconnect) {
            if (attempts < 12) {
                System.out.println("Channel关闭，将进行重连！");
                int timeout = 2 << attempts;
                timer.newTimeout(this, timeout, TimeUnit.SECONDS);
            }
        }
        // 调用下一个ChannelInboundHandler的channelInactive方法
        ctx.fireChannelInactive();
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        ChannelFuture future;
        synchronized (bootStarp) {
            bootStarp.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(handers());
                }
            });
            future = bootStarp.connect(host, port);
        }

        // 检测连接服务端的状态
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                boolean isSuccess = future.isSuccess();
                // 如果重连不成功
                if (!isSuccess) {
                    System.out.println("重连失败");
                    future.channel().pipeline().fireChannelInactive();
                } else {
                    System.out.println("重连成功");
                }
            }
        });
    }

}
