package pers.mingshan.netty.production.serializer;

import pers.mingshan.netty.production.serializer.protostuff.ProtostuffSerializer;

public final class SerializerHolder {
    private static final Serializer serializer = new ProtostuffSerializer();

    public static Serializer serializerImpl() {
        return serializer;
    }
}
