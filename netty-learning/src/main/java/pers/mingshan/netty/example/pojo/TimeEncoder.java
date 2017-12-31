package pers.mingshan.netty.example.pojo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class TimeEncoder extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        UnixTime time = (UnixTime) msg;
        ByteBuf buffer = ctx.alloc().buffer(4);
        buffer.writeLong(time.getValue());
        // 当编码后的数据被写到了通道上 Netty 可以通过ChannelPromise标记是成功还是失败
        ctx.write(buffer, promise);
        // 这里不必调用cxt.flush()
    }
}
