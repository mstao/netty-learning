package pers.mingshan.netty.example.heartbeat.improve;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

public class HeartBeatClient {
    protected final HashedWheelTimer timer = new HashedWheelTimer();
    private Bootstrap b;
    private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();

    public void connect(String host, int port) throws Exception {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        b = new Bootstrap();
        b.group(worker)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .handler(new LoggingHandler(LogLevel.INFO));

        ConnectionWatchDog watch = new ConnectionWatchDog(b, host, port, timer, true) {
            @Override
            public ChannelHandler[] handers() {
                return new ChannelHandler[] {
                        this,
                        new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS),
                        idleStateTrigger,
                        new StringDecoder(),
                        new StringEncoder(),
                        new HeartBeatClientHandler()
                };
            }
        };

        // 启动客户端
        ChannelFuture future;
        try {
            synchronized (b) {
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(watch.handers());
                    }
                });

                future = b.connect(host, port);
            }
            future.sync();
        } catch (Exception e) {
            throw new Exception("连接失败");
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;  
       if (args != null && args.length > 0) {  
           try {  
               port = Integer.valueOf(args[0]);  
           } catch (NumberFormatException e) {  
               // 采用默认值  
           }  
       }  
       new HeartBeatClient().connect("127.0.0.1", port);  
    }
}
