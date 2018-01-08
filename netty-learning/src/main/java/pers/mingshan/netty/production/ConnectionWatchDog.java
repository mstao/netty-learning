package pers.mingshan.netty.production;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import pers.mingshan.netty.example.heartbeat.improve.ChannelHanlderHolder;

/**
 * 用来监测 客户端 连接服务端的状态，断开连接后进行重连,
 * 默认重连12次
 * @author mingshan
 *
 */
public abstract class ConnectionWatchDog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHanlderHolder {
    protected static final Logger logger = LoggerFactory.getLogger(ConnectionWatchDog.class);
    // 辅助启动类
    private final Bootstrap bootStarp;
    // 地址
    private final String host;
    // 端口
    private final int port;
    // 定时器
    private final Timer timer;

    // 是否重连
    private volatile boolean reconnect = true;

    // 重试次数
    private int attempts;

    /**
     * 构造初始化
     * 
     * @param bootStarp
     * @param host
     * @param port
     * @param timer
     * @param reconnect
     */
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
        logger.info("Connects with {}.", ctx.channel());

        attempts = 0;
        // 调用下一个ChannelInboundHandler的channelActive方法
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        boolean doReconnect = reconnect;

        if (doReconnect) {
            if (attempts < 12) {
                logger.info("Disconnects with {} close, will reconnect!", ctx.channel());
                int timeout = 2 << attempts;
                timer.newTimeout(this, timeout, TimeUnit.SECONDS);
            }
        }

        logger.warn("Disconnects with {}, port: {},host {}, reconnect: {}.", ctx.channel(), port,host, doReconnect);
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
                boolean succeed = future.isSuccess();
                logger.warn("Reconnects with {}, {}.", host + ":" + port, succeed ? "succeed" : "failed");

                // 如果重连不成功
                if (!succeed) {
                    future.channel().pipeline().fireChannelInactive();
                }
            }
        });
    }

    /**
     * 设置是否重连，默认为true
     * @param reconnect
     */
    public void setReconnect(boolean reconnect) {
       this.reconnect = reconnect;
    }
}
