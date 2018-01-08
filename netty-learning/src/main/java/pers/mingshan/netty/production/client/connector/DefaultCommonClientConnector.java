package pers.mingshan.netty.production.client.connector;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import pers.mingshan.netty.production.ConnectionWatchDog;
import pers.mingshan.netty.production.common.Acknowledge;
import pers.mingshan.netty.production.common.Message;
import pers.mingshan.netty.production.common.NativeSupport;
import pers.mingshan.netty.production.common.exception.ConnectFailedException;
import pers.mingshan.netty.production.srv.acceptor.AcknowledgeEncoder;

public class DefaultCommonClientConnector extends NettyClientConnector {
    protected static final Logger logger = LoggerFactory.getLogger(DefaultCommonClientConnector.class);
    // 每一个连接维护一个channel
    private volatile Channel channel;
    // 信息处理的handler
    private final MessageHandler handler = new MessageHandler();
    // 编码
    private final MessageEncoder encoder = new MessageEncoder();
    // ack 编码
    private final AcknowledgeEncoder ackEncoder = new AcknowledgeEncoder();
    // 未发送ack的消息
    private final ConcurrentMap<Long, MessageNonAck> messageNonAcks = new ConcurrentHashMap<>();
    // 心跳触发器
    private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();

    private final HashedWheelTimer timer = new HashedWheelTimer(new ThreadFactory() {
        private AtomicInteger threadIndex = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "NettyClientConnectorExecutor_" + this.threadIndex.incrementAndGet());
        }
    });
  
    public DefaultCommonClientConnector() {
        init();
    }

    @Override
    protected void init() {
        super.init();

        bootstrap()
        .option(ChannelOption.ALLOCATOR, allocator)
        .option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
        .option(ChannelOption.SO_REUSEADDR, true)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) SECONDS.toMillis(3))
        .channel(NioSocketChannel.class);

        bootstrap().option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.ALLOW_HALF_CLOSURE, false);
    }

    @Override
    public Channel connect(String host, int port) {
        final Bootstrap bootstrap = bootstrap();
        // 重连机制
        ConnectionWatchDog watchDog = new ConnectionWatchDog(bootstrap, host, port, timer, false) {

            @Override
            public ChannelHandler[] handers() {
                return new ChannelHandler[] {
                        this,
                        // 每隔30s的时间触发一次userEventTriggered的方法，并且指定IdleState是WRITER_IDLE
                        new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS),
                        idleStateTrigger,
                        new MessageDecoder(),
                        encoder,
                        ackEncoder,
                        handler
                };
            }
        };

        watchDog.setReconnect(true);
        ChannelFuture future;
        try {
            synchronized (bootstrapLock()) {
                bootstrap.handler(new ChannelInitializer<Channel>() {
    
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(watchDog.handers());
                    }
                });

                future = bootstrap.connect(host, port);
            }

            future.sync();
            channel = future.channel();
        } catch (Throwable t) {
            throw new ConnectFailedException("Connect to [" + host + ":" + port + "] fails.", t);
        }

        return channel;
    }

    @Sharable
    class MessageHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Acknowledge) {
                logger.info("收到server端的Ack信息，无需再次发送信息");
                messageNonAcks.remove(((Acknowledge)msg).getSequence());
            }
        }
    }

    /**
     * 针对Linux epoll进行优化
     */
    @Override
    protected EventLoopGroup initEventLoopGroup(int nWorkers, DefaultThreadFactory workerFactory) {
        return NativeSupport.isSupportNativeET() 
                ? new EpollEventLoopGroup(nWorkers, workerFactory)
                : new NioEventLoopGroup(nWorkers, workerFactory);
    }

    public static class MessageNonAck {
        private final long id;

        private final Message msg;
        private final Channel channel;
        private final long timestamp = System.currentTimeMillis();

        public MessageNonAck(Message msg, Channel channel) {
            this.msg = msg;
            this.channel = channel;

            id = msg.getSequence();
        }
    }

    /**
     * 扫描超时的ack
     * @author mingshan
     *
     */
    private class AckTimeoutScanner implements Runnable {

        @Override
        public void run() {
            for (;;) {
                try {
                    for (MessageNonAck m : messageNonAcks.values()) {
                        // 如果ack 时间超过10秒
                        if (m.timestamp - System.currentTimeMillis() > SECONDS.toMillis(10)) {
                            // 将其从map中移除
                            if (messageNonAcks.remove(m.id) == null) {
                                continue;
                            }

                            if (m.channel.isActive()) {
                                // 重新发送ack
                                logger.warn("准备重新发送信息");
                                MessageNonAck msgNonAck = new MessageNonAck(m.msg, m.channel);
                                messageNonAcks.put(m.id, msgNonAck);
                                m.channel.writeAndFlush(msgNonAck)
                                    .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                            }
                        }
                    }
                    Thread.sleep(300);
                } catch (Throwable t) {
                    logger.error("An exception has been caught while scanning the timeout acknowledges {}.", t);
                }
            }
        }
    }

    /**
     * 启动ack超时扫描
     */
    {
        Thread t = new Thread(new AckTimeoutScanner(), "ack.scanner");
        t.setDaemon(true);
        t.start();
    }

    public void addNeedAckMessageInfo(MessageNonAck msgNonAck) {
        messageNonAcks.put(msgNonAck.id, msgNonAck);
    }
}
