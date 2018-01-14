package pers.mingshan.netty.production.example;

import static pers.mingshan.netty.production.common.NettyCommonProtocol.REQUEST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import pers.mingshan.netty.production.client.connector.DefaultCommonClientConnector;
import pers.mingshan.netty.production.client.connector.DefaultCommonClientConnector.MessageNonAck;
import pers.mingshan.netty.production.common.Message;

public class ClientConnectorStartup {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnectorStartup.class);

    public static void main(String[] args) {
        DefaultCommonClientConnector clientConnector = new DefaultCommonClientConnector();
        Channel channel = clientConnector.connect("127.0.0.1", 8078);
        User user = new User(1, "walker");
        Message message = new Message();
        message.setSign(REQUEST);
        message.setData(user);
        channel.writeAndFlush(message).addListener(new ChannelFutureListener() {
            
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(!future.isSuccess()) {  
                    logger.info("send fail,reason is {}", future.cause().getMessage());  
                } 
            }
        });

        //防止对象处理发生异常的情况
        MessageNonAck msgNonAck = new MessageNonAck(message, channel);
        clientConnector.addNeedAckMessageInfo(msgNonAck);
    }

    static class User {
        private Integer id;
        
        private String username;
        

        public User(Integer id, String username) {
            this.id = id;
            this.username = username;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return "User [id=" + id + ", username=" + username + "]";
        }
    }
}
