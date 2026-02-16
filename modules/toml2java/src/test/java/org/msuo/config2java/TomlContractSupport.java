package org.msuo.config2java;

public abstract class TomlContractSupport extends SharedContractSupport {
    private final Deserializer deserializer = new TomlDeserializer();

    @Override
    protected <T> T deserialize(String source, Class<T> cls) {
        return deserializer.deserialize(source, cls);
    }
}
