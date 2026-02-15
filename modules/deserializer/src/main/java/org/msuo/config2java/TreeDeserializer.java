package org.msuo.config2java;

public abstract class TreeDeserializer implements Deserializer {

    protected abstract ConfigValue parse(String source);

    @Override
    public final <T> T deserialize(String source, Class<T> configClass) {
        return Binder.deserialize(parse(source), configClass);
    }
}
