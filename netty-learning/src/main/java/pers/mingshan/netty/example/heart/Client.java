package pers.mingshan.netty.example.heart;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Netty 心跳检测 ，重连机制
 * 
 * @author mingshan
 *
 */
public class Client {
    static final String HOST = System.getProperty("host", "127.0.0.1");  
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));  

    public void conn() throws InterruptedException {
        // Configure the client.  
        EventLoopGroup group = new NioEventLoopGroup();  
        ChannelFuture future = null;
        
        try {  
            Bootstrap b = new Bootstrap();  
            b.group(group)  
             .channel(NioSocketChannel.class)  
             .option(ChannelOption.TCP_NODELAY, true)  
             .handler(new ChannelInitializer<SocketChannel>() {  
                 @Override  
                 public void initChannel(SocketChannel ch) throws Exception {  
                     ChannelPipeline p = ch.pipeline();  
                     // 添加心跳检测
                     p.addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                     p.addLast("decoder", new StringDecoder());
                     p.addLast("encoder", new StringEncoder());
                     p.addLast(new ClientHandler());
                 }
             });
  
            future = b.connect(HOST, PORT).sync();
            future.channel().writeAndFlush("Hello Netty Server ,I am a common client");  
            future.channel().closeFuture().sync();
        } finally {
            // 如果考虑到 超时重连，那么就不能直接关闭客户端了，需要进行重连处理
            // group.shutdownGracefully();

            if (future != null) {
                if (future.channel() != null && future.channel().isOpen()) {
                    future.channel().close();
                }
            }

            System.out.println("客户端开始重新连接-");
            conn();
            System.out.println("客户端重新连接成功 ！");
            
        }
    }

    public static void main(String[] args) throws Exception {  
        new Client().conn();
    }
}
