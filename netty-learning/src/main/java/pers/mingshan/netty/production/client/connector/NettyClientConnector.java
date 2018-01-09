package pers.mingshan.netty.production.client.connector;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * Netty客户端连接器
 * 
 * @author mingshan
 *
 */
public abstract class NettyClientConnector implements ClientConnector {
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private Bootstrap bootstrap;
    private EventLoopGroup worker;
    private int nWorkers;

    protected volatile ByteBufAllocator allocator;

    public NettyClientConnector() {
        this(AVAILABLE_PROCESSORS << 1);
    }

    public NettyClientConnector(int nWorker) {
        this.nWorkers = nWorker;
    }

    protected void init() {
        DefaultThreadFactory workerFactory = new DefaultThreadFactory("client.connector");
        worker = initEventLoopGroup(nWorkers, workerFactory);
        bootstrap = new Bootstrap().group(worker);
    }

    protected Bootstrap bootstrap() {
        return bootstrap;
    }

    protected Object bootstrapLock() {
        return bootstrap;
    }

    @Override
    public void shutdownGracefully() {
        worker.shutdownGracefully();
    }

    protected abstract EventLoopGroup initEventLoopGroup(int nWorkers, DefaultThreadFactory workerFactory);
}
