package org.msuo.config2java;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.Map;

public final class GroovyDeserializer extends TreeDeserializer {

    private final ScriptBindings bindings;

    public GroovyDeserializer() {
        this(ScriptBindings.empty());
    }

    public GroovyDeserializer(ScriptBindings bindings) {
        this.bindings = bindings;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected ConfigValue parse(String source) {
        try {
            Binding binding = new Binding();
            binding.setVariable(
                "ENV",
                EnvironmentValues.withSystemFallback(bindings.environment())
            );
            for (Map.Entry<String, Object> e : bindings.globals().entrySet()) {
                binding.setVariable(e.getKey(), e.getValue());
            }

            Object root = new GroovyShell(binding).evaluate(source);
            return new GroovyConfigValue(root);
        } catch (RuntimeException e) {
            throw new ConfigSourceException("Groovy", "evaluate", e.getMessage(), e);
        }
    }

    private static final class GroovyConfigValue extends MapListConfigValue {

        GroovyConfigValue(Object value) {
            super(value);
        }

        @Override
        protected ConfigValue wrap(Object value) {
            return new GroovyConfigValue(value);
        }
    }

    public static final class Builder {
        private final ScriptBindings.Builder bindings = ScriptBindings.builder();

        public Builder environment(Map<String, String> values) {
            this.bindings.environment(values);
            return this;
        }

        public Builder env(String key, String value) {
            this.bindings.env(key, value);
            return this;
        }

        public Builder globals(Map<String, ?> values) {
            this.bindings.globals(values);
            return this;
        }

        public Builder global(String key, Object value) {
            this.bindings.global(key, value);
            return this;
        }

        public GroovyDeserializer build() {
            return new GroovyDeserializer(bindings.build());
        }
    }
}
