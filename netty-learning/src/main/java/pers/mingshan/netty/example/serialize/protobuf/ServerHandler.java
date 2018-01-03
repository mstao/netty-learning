package pers.mingshan.netty.example.serialize.protobuf;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import pers.mingshan.netty.example.serialize.protobuf.AddressBookProtos.Person.PhoneNumber;

public class ServerHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        AddressBookProtos.Person person = (AddressBookProtos.Person) msg;
        System.out.println("person = " + person.toString());
        List<PhoneNumber> phones = person.getPhonesList();
        if (phones != null) {
            for(PhoneNumber phone : phones) {
                System.out.println("phone = " + phone.toString());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
