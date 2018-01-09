package pers.mingshan.netty.production.srv.acceptor;

import java.net.SocketAddress;

/**
 * 服务端接口定义
 * 
 * @author mingshan
 *
 */
public interface SrvAcceptor {

    /**
     * 
     * @return
     */
    SocketAddress localAddress();

    /**
     * 
     */
    void start() throws InterruptedException;

    /**
     * 
     * @param sync
     */
    void start(boolean sync) throws InterruptedException;

    /**
     * 优雅退出
     */
    void shutdownGracefully();
}
