package org.msuo.config2java;

public abstract class XmlContractSupport extends SharedContractSupport {
    private final Deserializer deserializer = new XmlDeserializer();

    @Override
    protected <T> T deserialize(String source, Class<T> cls) {
        return deserializer.deserialize(source, cls);
    }
}
