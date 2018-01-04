package pers.mingshan.netty.production;

import io.netty.channel.ChannelHandler;

/**
 * 客户端ChannelHandler的集合
 * 
 * @author mingshan
 *
 */
public interface ChannelHandlerHolder {

    ChannelHandler[] handers();
}
