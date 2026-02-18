package org.msuo.data2java;

public abstract class LuaContractSupport extends SharedContractSupport {
    private final Deserializer deserializer = new LuaDeserializer();

    @Override
    protected <T> T deserialize(String source, Class<T> cls) {
        return deserializer.deserialize(source, cls);
    }
}
