package org.msuo.config2java;

public abstract class JsonContractSupport extends SharedContractSupport {

    @Override
    protected <T> T deserialize(String source, Class<T> cls) {
        return JsonDeserializer.deserialize(source, cls);
    }
}
