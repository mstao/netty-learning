package pers.mingshan.netty.production.srv.acceptor;

import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pers.mingshan.netty.production.common.NettyEvent;
import pers.mingshan.netty.production.common.ServiceThread;

public abstract class DefaultSrvAcceptor extends NettySrvAcceptor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSrvAcceptor.class);

    protected final NettyEventExecuter eventExecutor = new NettyEventExecuter();

    public DefaultSrvAcceptor(SocketAddress localAddress) {
        super(localAddress);
    }

    public void putNettyEvent(final NettyEvent event) {
        this.eventExecutor.putNettyEvent(event);
    }

    class NettyEventExecuter extends ServiceThread {
        private static final int MAX_SIZE = 10000;
        private LinkedBlockingQueue<NettyEvent> eventQueue = new LinkedBlockingQueue<>();

        public void putNettyEvent(final NettyEvent event) {
            if (eventQueue.size() < MAX_SIZE) {
                eventQueue.add(event);
            } else {
                logger.warn("event queue size[{}] enough, so drop this event {}", this.eventQueue.size(),
                        event.toString());
            }
        }

        @Override
        public void run() {
            logger.info(this.getServiceName() + " service start!");

            final ChannelEventListener listener = DefaultSrvAcceptor.this.getChannelEventListener();
            // 轮询channel中的事件
            while (!this.isStoped()) {
                try {
                    NettyEvent event = eventQueue.poll(3000, TimeUnit.MILLISECONDS);

                    if (event != null && listener != null) {
                        switch (event.getType()) {
                            case IDLE:
                                listener.onChannelIdle(event.getRemoteAddr(), event.getChannel());
                                break;
                            case CLOSE:
                                listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
                                break;
                            case CONNECT:
                                listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
                                break;
                            case EXCEPTION:
                                listener.onChannelException(event.getRemoteAddr(), event.getChannel());
                                break;
                            default:
                                break;
                        }
                    }
                } catch (InterruptedException e) {
                    logger.warn(this.getServiceName() + " service has exception. ", e);
                }
            }
        }

        @Override
        protected String getServiceName() {
            return NettyEventExecuter.class.getSimpleName();
        }
    }

    protected abstract ChannelEventListener getChannelEventListener();
}
