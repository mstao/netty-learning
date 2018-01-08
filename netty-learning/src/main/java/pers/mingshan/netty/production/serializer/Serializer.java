package pers.mingshan.netty.production.serializer;

/**
 * 序列化接口
 * 
 * @author mingshan
 *
 */
public interface Serializer {

    /**
     * 将对象序列化为byte数组
     * 
     * @param obj 将要序列化的对象
     * @return 序列化后的byte数组
     */
    <T> byte[] writeObject(T obj);

    /**
     * 将byte数组反序列化成class是clazz的obj对象
     * 
     * @param bytes 序列化后的byte数组
     * @param clazz 要转成的类
     * @return 生成的对象
     */
    <T> T readObject(byte[] bytes, Class<T> clazz);
}
