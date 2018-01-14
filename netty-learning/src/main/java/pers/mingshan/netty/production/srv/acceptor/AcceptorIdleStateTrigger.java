package pers.mingshan.netty.production.srv.acceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 读超时触发器
 * 用于心跳检测
 * 
 * @author mingshan
 *
 */
@Sharable
public class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AcceptorIdleStateTrigger.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                logger.error("Occor exception");
                throw new Exception("NO SIGNAL");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
