package org.msuo.config2java;

public abstract class TomlContractSupport extends SharedContractSupport {

    @Override
    protected <T> T deserialize(String source, Class<T> cls) {
        return TomlDeserializer.deserialize(source, cls);
    }
}
