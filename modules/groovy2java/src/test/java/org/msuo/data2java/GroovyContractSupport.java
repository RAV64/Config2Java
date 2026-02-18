package org.msuo.data2java;

public abstract class GroovyContractSupport extends SharedContractSupport {
    private final Deserializer deserializer = new GroovyDeserializer();

    @Override
    protected <T> T deserialize(String source, Class<T> cls) {
        return deserializer.deserialize(source, cls);
    }
}
