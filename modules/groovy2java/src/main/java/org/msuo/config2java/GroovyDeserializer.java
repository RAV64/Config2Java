package org.msuo.config2java;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.Map;

public final class GroovyDeserializer extends AbstractScriptDeserializer {

    @Override
    protected ConfigValue parse(
        String source,
        Map<String, String> environment,
        Map<String, Object> globals
    ) {
        try {
            Binding binding = new Binding();
            binding.setVariable(
                "ENV",
                EnvironmentValues.withSystemFallback(environment)
            );
            for (Map.Entry<String, ?> e : globals.entrySet()) {
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
