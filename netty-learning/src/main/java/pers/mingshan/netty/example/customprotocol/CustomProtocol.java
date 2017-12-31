package pers.mingshan.netty.example.customprotocol;

import java.util.Arrays;

/**
 * 自定义协议 
 * 解决TCP 粘包，拆包问题
 * 
 * <pre>
 * 数据包格式:
 * +——-------——+——-------——+——-------——+
 * | 协议开始标志   |   数据长度      |    数据          |
 * +——-------——+——-------——+——-------——+
 * </pre>
 * 
 * 数据包各个部分含义如下：
 * <ul>
 *   <li>1. 协议开始标志是一个十六进制的魔数，代表消息的开始</li>
 *   <li>2. 传输数据的长度，int类型</li>
 *   <li>3. 具体的数据信息</li>
 * </ul>
 * @author mingshan
 *
 */
public class CustomProtocol {

    // 消息的开始标志
    private int headData = Constants.HEAD_DATA;

    // 消息的长度
    private int contentLength;

    // 具体的消息内容
    private byte[] content;

    /**
     * 构造函数初始化数据
     * @param contentLength 消息的长度
     * @param content 具体的消息内容
     */
    public CustomProtocol(int contentLength, byte[] content) {
        this.contentLength = contentLength;
        this.content = content;
    }

    public int getHeadData() {
        return headData;
    }

    public void setHeadData(int headData) {
        this.headData = headData;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "CustomProtocol [headData=" + headData + ", contentLength=" + contentLength + ", content="
                + Arrays.toString(content) + "]";
    }


}
