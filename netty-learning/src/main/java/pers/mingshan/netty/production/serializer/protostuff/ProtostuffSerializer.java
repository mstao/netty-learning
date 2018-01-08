package pers.mingshan.netty.production.serializer.protostuff;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import pers.mingshan.netty.production.serializer.Serializer;

/**
 * 使用Protostuff进行序列化
 * 
 * @author mingshan
 *
 */
public class ProtostuffSerializer implements Serializer {

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] writeObject(T obj) {

        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T readObject(byte[] bytes, Class<T> clazz) {
        try {
            Schema<T> schema = getSchema(clazz);
            T message = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(bytes, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * 这里简化获取schema 的方式，不需要在这个类中来缓存生成的schema，
     * 因为在{@link RuntimeSchema} 会自动缓存
     * @param cls
     * @return
     */
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = RuntimeSchema.getSchema(cls);
        return schema;
    }

}
