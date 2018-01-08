package pers.mingshan.netty.production.common;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息包装类
 * 
 * @author mingshan
 *
 */
public class Message {
    private static final AtomicLong sequenceGenerator = new AtomicLong(0);

    private final long sequence;
    private short sign;
    private long version; // 版本号
    private Object data;

    public Message() {
        this(sequenceGenerator.getAndIncrement());
    }

    public Message(long sequence) {
        this.sequence = sequence;
    }

    public short getSign() {
        return sign;
    }

    public void setSign(short sign) {
        this.sign = sign;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getSequence() {
        return sequence;
    }

    @Override
    public String toString() {
        return "Message [sequence=" + sequence + ","
                + " sign=" + sign
                + ", version=" + version
                + ", data=" + data + "]";
    }
}
