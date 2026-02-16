package org.msuo.config2java;

public abstract class LuaContractSupport extends SharedContractSupport {

    @Override
    protected <T> T deserialize(String source, Class<T> cls) {
        return LuaDeserializer.deserialize(source, cls);
    }
}
