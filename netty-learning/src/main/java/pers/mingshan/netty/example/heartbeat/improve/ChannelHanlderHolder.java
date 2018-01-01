package pers.mingshan.netty.example.heartbeat.improve;

import io.netty.channel.ChannelHandler;

/**
 * 客户端ChannelHandler的集合
 * 
 * @author mingshan
 *
 */
public interface ChannelHanlderHolder {

    /**
     * 
     * @return {@link ChannelHandler}
     */
    ChannelHandler[] handers();
}
