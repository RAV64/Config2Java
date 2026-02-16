package org.msuo.config2java;

public abstract class XmlContractSupport extends SharedContractSupport {

    @Override
    protected <T> T deserialize(String source, Class<T> cls) {
        return XmlDeserializer.deserialize(source, cls);
    }
}
