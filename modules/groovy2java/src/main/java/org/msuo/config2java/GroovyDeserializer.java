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

    public static ScriptedDeserializerBuilder<GroovyDeserializer> builder() {
        return ScriptedDeserializerBuilder.of(GroovyDeserializer::new);
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

}
