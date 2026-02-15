package org.msuo.config2java;

import java.util.Map;
import java.util.function.Function;

public final class ScriptedDeserializerBuilder<T> {

    private final ScriptBindings.Builder bindings = ScriptBindings.builder();
    private final Function<ScriptBindings, T> factory;

    private ScriptedDeserializerBuilder(Function<ScriptBindings, T> factory) {
        this.factory = factory;
    }

    public static <T> ScriptedDeserializerBuilder<T> of(Function<ScriptBindings, T> factory) {
        return new ScriptedDeserializerBuilder<>(factory);
    }

    public ScriptedDeserializerBuilder<T> environment(Map<String, String> values) {
        bindings.environment(values);
        return this;
    }

    public ScriptedDeserializerBuilder<T> env(String key, String value) {
        bindings.env(key, value);
        return this;
    }

    public ScriptedDeserializerBuilder<T> globals(Map<String, ?> values) {
        bindings.globals(values);
        return this;
    }

    public ScriptedDeserializerBuilder<T> global(String key, Object value) {
        bindings.global(key, value);
        return this;
    }

    public T build() {
        return factory.apply(bindings.build());
    }
}
