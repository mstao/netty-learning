package pers.mingshan.netty.production.client.connector;

import static pers.mingshan.netty.production.common.NettyCommonProtocol.ACK;
import static pers.mingshan.netty.production.common.NettyCommonProtocol.MAGIC;
import static pers.mingshan.netty.production.common.NettyCommonProtocol.RESPONSE;
import static pers.mingshan.netty.production.serializer.SerializerHolder.serializerImpl;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import pers.mingshan.netty.production.common.Acknowledge;
import pers.mingshan.netty.production.common.Message;
import pers.mingshan.netty.production.common.NettyCommonProtocol;


public class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {
    private NettyCommonProtocol header = new NettyCommonProtocol();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch(state()) {
            case HEADER_MAGIC : 
                checkMagic(in.readShort());
                checkpoint(State.HEADER_SIGN);
            case HEADER_SIGN :
                header.setSign(in.readByte());
                checkpoint(State.HEADER_STATUS);
            case HEADER_STATUS :
                header.setStatus(in.readByte());
                checkpoint(State.HEADER_ID);
            case HEADER_ID:
                header.setId(in.readLong());
                checkpoint(State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                header.setBodyLength(in.readInt());
                checkpoint(State.BODY);
            case BODY : 
                switch(header.getSign()) {
                    case RESPONSE: {
                        // 将body内容存到byte数组中
                        byte[] body = new byte[header.getBodyLength()];
                        in.readBytes(body);
                        // 反序列化
                        Message message = serializerImpl().readObject(body, Message.class);
                        message.setSign(header.getSign());
                        out.add(message);
                        break;
                    }
                    case ACK: {
                        // 将body内容存到byte数组中
                        byte[] body = new byte[header.getBodyLength()];
                        in.readBytes(body);
                        // 反序列化
                        Acknowledge ack = serializerImpl().readObject(body, Acknowledge.class);
                        out.add(ack);
                        break;
                    }
                    default:
                        throw new IllegalArgumentException();
                }
                checkpoint(State.HEADER_MAGIC);
        default:
            throw new IllegalArgumentException();
        }
    }

    private void checkMagic(short magic) {
        if (magic != MAGIC) {
            throw new IllegalArgumentException("Invaid magic!");
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
