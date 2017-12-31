package pers.mingshan.netty.example.customprotocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 自定义解码器
 * @author mingshan
 *
 */
public class CustomDecoder extends ByteToMessageDecoder {
    // 一个数据包的基本长度，即数据内容为空
    // 包含 两部分， 协议标志(占4字节)和消息长度(占4字节)
    private static final int BASE_LENGTN = 4 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 可读长度需要大于数据包的基本长度
        if (in.readableBytes() > BASE_LENGTN) {
            // 防止socket字节流攻击  
            // 防止，客户端传来的数据过大  
            // 因为，太大的数据，是不合理的  
            if (in.readableBytes() > 2048) {
                in.skipBytes(in.readableBytes());
            }

            // 记录数据包开始的位置
            int beginReader;
            // 检测是否有数据可读
            while (true) {
                // 获取数据包的开始位置
                beginReader = in.readerIndex();
                // 标记包头的开始位置
                in.markReaderIndex();
                // 如果读到了协议的开始标志，那么就结束循环
                // 接下来就要读取消息的内容了
                if (in.readInt() == Constants.HEAD_DATA) {
                    break;
                }

                // 未读到包头，略过一个字节  
                // 每次略过，一个字节，去读取，包头信息的开始标记  
                in.resetReaderIndex();
                in.readByte();
  
                // 当略过，一个字节之后
                // 数据包的长度，又变得不满足  
                // 此时，应该结束。等待后面的数据到达  
                if (in.readableBytes() < BASE_LENGTN) {
                    return;
                }
            }

            // 消息的长度
            int dataLength = in.readInt();
            // 读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex.
            // 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
            if (in.readableBytes() < dataLength) {
                in.readerIndex(beginReader);
                return;
            }

            byte[] body = new byte[dataLength];
            in.readBytes(body);
            CustomProtocol protocol = new CustomProtocol(dataLength, body);
            out.add(protocol);
        }
    }

}
