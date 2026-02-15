package org.msuo.config2java;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.Iterator;
import java.util.List;
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
            binding.setVariable("ENV", bindings.environment());
            for (Map.Entry<String, Object> e : bindings.globals().entrySet()) {
                binding.setVariable(e.getKey(), e.getValue());
            }

            Object root = new GroovyShell(binding).evaluate(source);
            return new GroovyConfigValue(root);
        } catch (RuntimeException e) {
            throw new ConfigSourceException("Groovy", "evaluate", e.getMessage(), e);
        }
    }

    private static final class GroovyConfigValue implements ConfigValue {

        private final Object value;

        GroovyConfigValue(Object value) {
            this.value = value;
        }

        @Override
        public String typename() {
            if (value == null) return "nil";
            if (value instanceof Map || value instanceof List) return "table";
            if (value instanceof CharSequence) return "string";
            if (value instanceof Boolean) return "boolean";
            if (value instanceof Number) return "number";
            return "userdata";
        }

        @Override
        public boolean isNil() {
            return value == null;
        }

        @Override
        public boolean isTable() {
            return value instanceof Map || value instanceof List;
        }

        @Override
        public ConfigTable asTable() {
            if (value instanceof Map) return new GroovyMapTable((Map<?, ?>) value);
            if (value instanceof List) return new GroovyListTable((List<?>) value);
            throw new IllegalStateException("not a table value");
        }

        @Override
        public ScalarValue asScalar() {
            if (value == null) return null;
            if (value instanceof CharSequence) return ScalarValue.ofString(value.toString());
            if (value instanceof Boolean) return ScalarValue.ofBoolean((Boolean) value);
            if (value instanceof Number) return ScalarNumbers.fromNumber((Number) value);
            return null;
        }
    }

    private static final class GroovyMapTable implements ConfigTable {

        private final Map<?, ?> map;

        GroovyMapTable(Map<?, ?> map) {
            this.map = map;
        }

        @Override
        public ConfigValue getField(String key) {
            if (!map.containsKey(key)) return MissingValue.INSTANCE;
            return new GroovyConfigValue(map.get(key));
        }

        @Override
        public Iterable<ConfigEntry> entries() {
            return () -> new Iterator<ConfigEntry>() {
                private final Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public ConfigEntry next() {
                    Map.Entry<?, ?> e = it.next();
                    ConfigValue key = e.getKey() == null
                        ? MissingValue.INSTANCE
                        : new GroovyConfigValue(e.getKey());
                    return ConfigEntry.of(
                        key,
                        new GroovyConfigValue(e.getValue()),
                        String.valueOf(e.getKey())
                    );
                }
            };
        }
    }

    private static final class GroovyListTable implements ConfigTable {

        private final List<?> list;

        GroovyListTable(List<?> list) {
            this.list = list;
        }

        @Override
        public int length() {
            return list.size();
        }

        @Override
        public ConfigValue getIndex(int index1Based) {
            if (index1Based < 1 || index1Based > list.size()) return MissingValue.INSTANCE;
            Object value = list.get(index1Based - 1);
            return new GroovyConfigValue(value);
        }

        @Override
        public Iterable<ConfigEntry> entries() {
            return () -> new Iterator<ConfigEntry>() {
                private int index0 = 0;

                @Override
                public boolean hasNext() {
                    return index0 < list.size();
                }

                @Override
                public ConfigEntry next() {
                    int index1 = index0 + 1;
                    Object value = list.get(index0);
                    index0++;
                    return ConfigEntry.of(
                        new GroovyConfigValue(index1),
                        new GroovyConfigValue(value),
                        String.valueOf(index1)
                    );
                }
            };
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
