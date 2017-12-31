package pers.mingshan.netty.example.customprotocol.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import pers.mingshan.netty.example.customprotocol.CustomDecoder;
import pers.mingshan.netty.example.customprotocol.CustomEncoder;

/**
 * TCP 拆包示例 - 客户端
 * 
 * 这里采用自定义协议来处理TCP 拆包/粘包问题
 * 
 * @author mingshan
 *
 */
public class Client {
    static final String HOST = System.getProperty("host", "127.0.0.1");  
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));  
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));  

    public static void main(String[] args) throws Exception {  
  
        // Configure the client.  
        EventLoopGroup group = new NioEventLoopGroup();  
        try {  
            Bootstrap b = new Bootstrap();  
            b.group(group)  
             .channel(NioSocketChannel.class)  
             .option(ChannelOption.TCP_NODELAY, true)  
             .handler(new ChannelInitializer<SocketChannel>() {  
                 @Override  
                 public void initChannel(SocketChannel ch) throws Exception {  
                     ChannelPipeline p = ch.pipeline();  
                     // 这里 添加自己定义编码解码器
                     p.addLast(new CustomDecoder());
                     p.addLast(new CustomEncoder());
                     p.addLast(new ClientHandler());
                 }  
             });  
  
            ChannelFuture future = b.connect(HOST, PORT).sync();
            future.channel().writeAndFlush("Hello Netty Server ,I am a common client");  
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }  
}
