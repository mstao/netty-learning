package pers.mingshan.netty.example.customprotocol.test;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import pers.mingshan.netty.example.customprotocol.CustomDecoder;
import pers.mingshan.netty.example.customprotocol.CustomEncoder;

/**
 * TCP 拆包/粘包示例 - 服务端
 * 
 * 这里采用自定义协议来处理TCP 拆包/粘包问题
 * 
 * @author mingshan
 *
 */
public class Server {
    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void start(){
        // EventLoopGroup用来处理I/O操作的多线程事件循环器
        // boss 用来接收连接
        // worker用来处理连接
        // 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(); 
        try {
            // ServerBootstrap是一个启动NIO服务的辅助启动类
            ServerBootstrap sbs = new ServerBootstrap();
            sbs.group(bossGroup,workerGroup)
            .channel(NioServerSocketChannel.class)
            .localAddress(new InetSocketAddress(port))
            .childHandler(new ChannelInitializer<SocketChannel>() {

                protected void initChannel(SocketChannel ch) throws Exception {
                    // 这里 添加自己定义编码解码器
                    ch.pipeline().addLast(new CustomDecoder());
                    ch.pipeline().addLast(new CustomEncoder());
                    ch.pipeline().addLast(new ServerHandler());
                };  
            })
            .option(ChannelOption.SO_BACKLOG, 1024) // 设置TCP缓冲区
            .childOption(ChannelOption.SO_KEEPALIVE, true); // childOption() 是提供给由父管道 ServerChannel 接收到的连接
             // 绑定端口，开始接收进来的连接  
             ChannelFuture future = sbs.bind(port).sync();

             System.out.println("Server start listen at " + port );
             future.channel().closeFuture().sync();
        } catch (Exception e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080; 
        }
        new Server(port).start();
    }
}
