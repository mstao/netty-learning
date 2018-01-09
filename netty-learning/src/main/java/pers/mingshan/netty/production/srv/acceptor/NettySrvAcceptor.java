package pers.mingshan.netty.production.srv.acceptor;

import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;

public abstract class NettySrvAcceptor implements SrvAcceptor {
    private static final Logger logger = LoggerFactory.getLogger(NettySrvAcceptor.class);
    // 可用的处理器个数
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    protected final SocketAddress localAddress;

    private ServerBootstrap bootstrap;
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private int nWorkers;

    protected volatile ByteBufAllocator allocator;

    public NettySrvAcceptor(SocketAddress localAddress) {
        this(localAddress, AVAILABLE_PROCESSORS << 1);
    }

    public NettySrvAcceptor(SocketAddress localAddress, int nWorkers) {
        this.localAddress = localAddress;
        this.nWorkers = nWorkers;
    }

    protected void init() {
        DefaultThreadFactory bossFactory = new DefaultThreadFactory("server.boss");
        DefaultThreadFactory workerFactory = new DefaultThreadFactory("server.worker");
        boss = initEventLoopGroup(1, bossFactory);
        worker = initEventLoopGroup(nWorkers, workerFactory);

        // 使用池化的directBuffer
        /**
         * 一般高性能的场景下,使用的堆外内存，也就是直接内存，使用堆外内存的好处就是减少内存的拷贝，和上下文的切换，缺点是
         * 堆外内存处理的不好容易发生堆外内存OOM
         * 当然也要看当前的JVM是否只是使用堆外内存，换而言之就是是否能够获取到Unsafe对象#PlatformDependent.directBufferPreferred()
         */
        allocator = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());
        bootstrap = new ServerBootstrap().group(boss, worker);
        // ByteBufAllocator 配置
        bootstrap.childOption(ChannelOption.ALLOCATOR, allocator);
    }

    @Override
    public void start() throws InterruptedException {
        this.start(true);
    }

    @Override
    public void start(boolean sync) throws InterruptedException {
        // 绑定端口，开始接收进来的连接  
        ChannelFuture future = bind(localAddress).sync();

        logger.info("netty acceptor server start at " + localAddress.toString());

        if (sync) {
            future.channel().closeFuture().sync();
        }
    }

    @Override
    public SocketAddress localAddress() {
        return localAddress;
    }

    @Override
    public void shutdownGracefully() {
        // 优雅关闭
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }

    protected abstract EventLoopGroup initEventLoopGroup(int nWorkers, DefaultThreadFactory workerFactory);

    protected abstract ChannelFuture bind(SocketAddress localAddress);
}
