package pers.mingshan.netty.production.srv.acceptor;

import io.netty.channel.Channel;

/**
 * Channel中的事件监听类
 * 
 * @author mingshan
 *
 */
public interface ChannelEventListener {
    void onChannelConnect(final String remoteAddr, final Channel channel);

    void onChannelClose(final String remoteAddr, final Channel channel);

    void onChannelException(final String remoteAddr, final Channel channel);

    void onChannelIdle(final String remoteAddr, final Channel channel);
}
