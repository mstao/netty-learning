package pers.mingshan.netty.production.srv.acceptor;

import java.net.SocketAddress;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * 基本的常用的netty Server配置
 * 
 * @author mingshan
 */
public class DefaultCommonSrvAcceptor extends DefaultSrvAcceptor {

    public DefaultCommonSrvAcceptor(SocketAddress localAddress) {
        super(localAddress);
    }

    @Override
    protected void init() {
        // TODO Auto-generated method stub
        super.init();
    }

    @Override
    public ChannelEventListener getChannelEventListener() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected EventLoopGroup initEventLoopGroup(int nWorkers, DefaultThreadFactory workerFactory) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ChannelFuture bind(SocketAddress localAddress) {
        // TODO Auto-generated method stub
        return null;
    }

}
