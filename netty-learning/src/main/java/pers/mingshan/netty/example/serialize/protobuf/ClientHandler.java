package pers.mingshan.netty.example.serialize.protobuf;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import pers.mingshan.netty.example.serialize.protobuf.AddressBookProtos.Person.PhoneNumber;
import pers.mingshan.netty.example.serialize.protobuf.AddressBookProtos.Person.PhoneType;

public class ClientHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelActive(ChannelHandlerContext ctx) {  
        AddressBookProtos.Person.Builder builder = AddressBookProtos.Person.newBuilder();
        builder.setId(1);
        builder.setEmail("499445428@qq.com");
        builder.setName("walker");
        
        List<AddressBookProtos.Person.PhoneNumber> phones = new ArrayList<> ();
        PhoneNumber phone1 = AddressBookProtos.Person.PhoneNumber.newBuilder()
                .setNumber("1111111").setType(PhoneType.HOME).build();
        PhoneNumber phone2 = AddressBookProtos.Person.PhoneNumber.newBuilder()
                .setNumber("2222222").setType(PhoneType.MOBILE).build();
        PhoneNumber phone3 = AddressBookProtos.Person.PhoneNumber.newBuilder()
                .setNumber("3333333").setType(PhoneType.WORK).build();
        
        phones.add(phone1);
        phones.add(phone2);
        phones.add(phone3);

        builder.addAllPhones(phones);
        // 写数据
        ctx.writeAndFlush(builder.build());
    }  
  
    @Override  
    public void channelRead(ChannelHandlerContext ctx, Object msg) {  
        System.out.println("HelloWorldClientHandler read Message:"+msg);  
    }

    @Override  
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {  
        cause.printStackTrace();  
        ctx.close();  
    }  
}
