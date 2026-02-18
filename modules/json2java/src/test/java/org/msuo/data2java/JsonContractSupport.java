package org.msuo.data2java;

public abstract class JsonContractSupport extends SharedContractSupport {
    private final Deserializer deserializer = new JsonDeserializer();

    @Override
    protected <T> T deserialize(String source, Class<T> cls) {
        return deserializer.deserialize(source, cls);
    }
}
