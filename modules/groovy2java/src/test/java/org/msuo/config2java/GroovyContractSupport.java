package org.msuo.config2java;

public abstract class GroovyContractSupport extends SharedContractSupport {

    @Override
    protected <T> T deserialize(String source, Class<T> cls) {
        return GroovyDeserializer.deserialize(source, cls);
    }
}
