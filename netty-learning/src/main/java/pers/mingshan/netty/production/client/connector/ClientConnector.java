package pers.mingshan.netty.production.client.connector;

import io.netty.channel.Channel;

/**
 * 
 * @author mingshan
 *
 */
public interface ClientConnector {

    /**
     * 连接服务端
     * @param host 
     * @param port
     * @return {@link Channel}
     */
    Channel connect(String host, int port);

    /**
     * 优雅退出
     */
    void shutdownGracefully();
}
