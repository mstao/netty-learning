package pers.mingshan.netty.example.customprotocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 自定义编码器
 * @author mingshan
 *
 */
public class CustomEncoder extends MessageToByteEncoder<CustomProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, CustomProtocol msg, ByteBuf out) throws Exception {
        // 写入消息的具体内容
        // 1.写入消息的开头的信息标志(十六进制,int类型)
        out.writeInt(msg.getHeadData());
        // 2.写入消息的长度(int 类型)  
        out.writeInt(msg.getContentLength());
        // 3.写入消息的内容(byte[]类型)
        out.writeBytes(msg.getContent());
    }

}
