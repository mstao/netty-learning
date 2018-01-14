package pers.mingshan.netty.production.srv.acceptor;

import static pers.mingshan.netty.production.common.NettyCommonProtocol.HEARTBEAT;
import static pers.mingshan.netty.production.common.NettyCommonProtocol.MAGIC;
import static pers.mingshan.netty.production.common.NettyCommonProtocol.REQUEST;
import static pers.mingshan.netty.production.common.NettyCommonProtocol.SERVICE_1;
import static pers.mingshan.netty.production.common.NettyCommonProtocol.SERVICE_2;
import static pers.mingshan.netty.production.common.NettyCommonProtocol.SERVICE_3;
import static pers.mingshan.netty.production.common.NettyCommonProtocol.SERVICE_4;
import static pers.mingshan.netty.production.serializer.SerializerHolder.serializerImpl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Signal;
import io.netty.util.concurrent.DefaultThreadFactory;
import pers.mingshan.netty.production.common.Acknowledge;
import pers.mingshan.netty.production.common.Message;
import pers.mingshan.netty.production.common.NativeSupport;
import pers.mingshan.netty.production.common.NettyCommonProtocol;
import pers.mingshan.netty.production.common.NettyEvent;
import pers.mingshan.netty.production.common.NettyEventType;

/**
 * 基本的常用的netty Server配置
 * 
 * @author mingshan
 */
public class DefaultCommonSrvAcceptor extends DefaultSrvAcceptor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCommonSrvAcceptor.class);

    // 连接心跳触发器
    private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();

    // message的编码器
    private final MessageEncoder encoder = new MessageEncoder();
    
    // Ack的编码器
    private final AcknowledgeEncoder ackEncoder = new AcknowledgeEncoder();
    
    // SimpleChannelInboundHandler类型的handler只处理@{link Message}类型的数据
    private final MessageHandler handler = new MessageHandler();

    // Channel事件监听
    private final ChannelEventListener channelEventListener;

    public DefaultCommonSrvAcceptor(int port, ChannelEventListener channelEventListener) {
        super(new InetSocketAddress(port));
        this.init();
        this.channelEventListener = channelEventListener;
    }

    @Override
    protected void init() {
        super.init();
        ServerBootstrap bootstarp = bootstrap();
        bootstarp.option(ChannelOption.SO_BACKLOG, 32768)
        .option(ChannelOption.SO_REUSEADDR, true)
        .childOption(ChannelOption.SO_REUSEADDR, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);
    }

    @Override
    protected ChannelEventListener getChannelEventListener() {
        return channelEventListener;
    }

    @Override
    protected EventLoopGroup initEventLoopGroup(int nThreads, DefaultThreadFactory threadFactory) {
        return NativeSupport.isSupportNativeET() ? new EpollEventLoopGroup(nThreads, threadFactory)
                                                 : new NioEventLoopGroup(nThreads, threadFactory);
    }

    @Override
    protected ChannelFuture bind(SocketAddress localAddress) {
        ServerBootstrap bootstrap = bootstrap();
        
        bootstrap.channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(
                        //每隔60s的时间内如果没有接受到任何的read事件的话，则会触发userEventTriggered事件，并指定IdleState的类型为READER_IDLE
                        new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS),
                        idleStateTrigger,
                        //message的解码器
                        new MessageDecoder(),
                        encoder,
                        ackEncoder,
                        new NettyConnetManageHandler(),
                        handler);
            }
        });

        return bootstrap.bind(localAddress);
    }

    /**
     * 消息编码
     * 
     * **************************************************************************************************
     *                                          Protocol
     *  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
     *       2   │   1   │    1   │     8     │      4      │
     *  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
     *           │       │        │           │             │
     *  │  MAGIC   Sign    Status   Invoke Id   Body Length                   Body Content              │
     *           │       │        │           │             │
     *  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
     *
     * 消息头16个字节定长
     * = 2 // MAGIC = (short) 0xbabe
     * + 1 // 消息标志位, 用来表示消息类型
     * + 1 // 空
     * + 8 // 消息 id long 类型
     * + 4 // 消息体body长度, int类型
     * 
     * @author mingshan
     *
     */
    @Sharable
    static class MessageEncoder extends MessageToByteEncoder<Message> {

        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
            byte[] bytes = serializerImpl().writeObject(msg);

            out.writeShort(MAGIC)
                .writeByte(msg.getSign())
                .writeByte(0)
                .writeLong(0)
                .writeInt(bytes.length)
                .writeBytes(bytes);
        }
    }

    /**
     * 消息解码
     * 
     * @author mingshan
     *
     */
    static class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {
        // 协议头
        private final NettyCommonProtocol header = new NettyCommonProtocol();

        // 构造函数 设置初始的枚举类型是什么
        public MessageDecoder() {
            super(State.HEADER_MAGIC);
        }


        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            switch (state()) {
                case HEADER_MAGIC:
                    checkMagic(in.readShort());             // MAGIC
                    checkpoint(State.HEADER_SIGN);
                case HEADER_SIGN:
                    header.setSign(in.readByte());             // 消息标志位
                    checkpoint(State.HEADER_STATUS);
                case HEADER_STATUS:
                    in.readByte();                          // no-op
                    checkpoint(State.HEADER_ID);
                case HEADER_ID:
                    header.setId(in.readLong());               // 消息id
                    checkpoint(State.HEADER_BODY_LENGTH);
                case HEADER_BODY_LENGTH:
                    header.setBodyLength(in.readInt());        // 消息体长度
                    checkpoint(State.BODY);
                case BODY:
                    switch (header.getSign()) {
                        case HEARTBEAT:
                            break;
                        case REQUEST:
                        case SERVICE_1:
                        case SERVICE_2:
                        case SERVICE_3:
                        case SERVICE_4: {
                            byte[] bytes = new byte[header.getBodyLength()];
                            in.readBytes(bytes);

                            Message msg = serializerImpl().readObject(bytes, Message.class);
                            msg.setSign(header.getSign());
                            out.add(msg);

                            break;
                        }
                        default:
                            throw new IllegalAccessException();
                    }
                    checkpoint(State.HEADER_MAGIC);
            }
        }
        private static void checkMagic(short magic) throws Signal {
            if (MAGIC != magic) {
                throw new IllegalArgumentException();
            }
        }

        enum State {
            HEADER_MAGIC,
            HEADER_SIGN,
            HEADER_STATUS,
            HEADER_ID,
            HEADER_BODY_LENGTH,
            BODY
        }
    }

    /**
     * 接收到消息，发送ack给客户端
     * 
     * @author mingshan
     *
     */
    @Sharable
    class MessageHandler extends SimpleChannelInboundHandler<Message> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
            Channel channel = ctx.channel();
            logger.info("receive message: {}", msg.toString());

            // 收到了消息，需要向客户端发送回执
            channel.writeAndFlush(new Acknowledge(msg.getSequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);;
        }
    }

    /**
     * Netty 有关连接事件处理
     * 
     * @author mingshan
     *
     */
    class NettyConnetManageHandler extends ChannelDuplexHandler {

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                ChannelPromise future) throws Exception {
            final String local = localAddress == null ? "UNKNOW" : localAddress.toString();
            final String remote = remoteAddress == null ? "UNKNOW" : remoteAddress.toString();
            logger.info("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);
            super.connect(ctx, remoteAddress, localAddress, future);

            if (DefaultCommonSrvAcceptor.this.channelEventListener != null) {
                DefaultCommonSrvAcceptor.this.putNettyEvent(new NettyEvent(NettyEventType.CONNECT, remoteAddress.toString(),
                        ctx.channel()));
            }
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
            final String remoteAddress = ctx.channel().remoteAddress().toString();
            logger.info("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
            super.disconnect(ctx, future);

            if (DefaultCommonSrvAcceptor.this.channelEventListener != null) {
                DefaultCommonSrvAcceptor.this.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress
                    .toString(), ctx.channel()));
            }
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            final String remoteAddress = ctx.channel().remoteAddress().toString();
            logger.info("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
            super.close(ctx, promise);

            if (DefaultCommonSrvAcceptor.this.channelEventListener != null) {
                DefaultCommonSrvAcceptor.this.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress
                    .toString(), ctx.channel()));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddress = ctx.channel().remoteAddress().toString();
            logger.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
            logger.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
            if (DefaultCommonSrvAcceptor.this.channelEventListener != null) {
                DefaultCommonSrvAcceptor.this.putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress
                    .toString(), ctx.channel()));
            }
        }
    }
}
