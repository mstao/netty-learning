package pers.mingshan.netty.production.srv.acceptor;

import static pers.mingshan.netty.production.common.NettyCommonProtocol.MAGIC;
import static pers.mingshan.netty.production.common.NettyCommonProtocol.ACK;
import static pers.mingshan.netty.production.serializer.SerializerHolder.serializerImpl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import pers.mingshan.netty.production.common.Acknowledge;

/**
 * Acknowledge 
 * @author mingshan
 *
 */
@Sharable
public class AcknowledgeEncoder extends MessageToByteEncoder<Acknowledge> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Acknowledge ack, ByteBuf out) throws Exception {
        byte[] bytes = serializerImpl().writeObject(ack);

        out.writeShort(MAGIC)
        .writeByte(ACK)
        .writeByte(0)
        .writeLong(ack.getSequence())
        .writeInt(bytes.length)
        .writeBytes(bytes);
    }

}
