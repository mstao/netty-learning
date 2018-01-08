package pers.mingshan.netty.production.common;

/**
 * Ack 消息确认
 * 
 * @author mingshan
 *
 */
public class Acknowledge {
    // ack序号
    private long sequence;

    public Acknowledge() {}

    public Acknowledge(long sequence) {
        this.sequence = sequence;
    }
    
    public long getSequence() {
        return sequence;
    }
    
    public void setSequence(long sequnece) {
        this.sequence = sequnece;
    }
}
