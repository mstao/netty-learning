package pers.mingshan.netty.example.pojo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TimeClient {
    static final String HOST = System.getProperty("host", "127.0.0.1");  
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));  

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(worker)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new TimeDecoder(), new TimeClientHandler());
                }
            });

            // 启动客户端
            ChannelFuture f = b.connect(HOST, PORT).sync();
            // 等待关闭连接
            f.channel().closeFuture().sync();
        } finally {
            worker.shutdownGracefully();
        }
    }
}
