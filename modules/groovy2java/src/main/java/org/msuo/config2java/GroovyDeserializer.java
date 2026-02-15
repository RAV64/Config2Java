package org.msuo.config2java;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GroovyDeserializer extends TreeDeserializer {

    private final Map<String, String> environment;
    private final Map<String, Object> globals;

    public GroovyDeserializer() {
        this(Collections.emptyMap(), Collections.emptyMap());
    }

    private GroovyDeserializer(Map<String, String> environment, Map<String, Object> globals) {
        this.environment = Collections.unmodifiableMap(new LinkedHashMap<>(environment));
        this.globals = Collections.unmodifiableMap(new LinkedHashMap<>(globals));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected ConfigValue parse(String source) {
        try {
            Binding binding = new Binding();
            binding.setVariable("ENV", environment);
            for (Map.Entry<String, Object> e : globals.entrySet()) {
                binding.setVariable(e.getKey(), e.getValue());
            }

            Object root = new GroovyShell(binding).evaluate(source);
            return new GroovyConfigValue(root);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to evaluate Groovy: " + e.getMessage(), e);
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
            if (value instanceof Number) return numberToScalar((Number) value);
            return null;
        }

        private static ScalarValue numberToScalar(Number n) {
            if (n instanceof Integer || n instanceof Short || n instanceof Byte) {
                return ScalarValue.ofInt(n.intValue());
            }
            if (n instanceof Long) {
                long l = n.longValue();
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                    return ScalarValue.ofInt((int) l);
                }
                return ScalarValue.ofDouble((double) l);
            }
            if (n instanceof Float || n instanceof Double) {
                double d = n.doubleValue();
                if (d == Math.rint(d) && d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                    return ScalarValue.ofInt((int) d);
                }
                return ScalarValue.ofDouble(d);
            }
            if (n instanceof BigDecimal) {
                BigDecimal bd = (BigDecimal) n;
                try {
                    return ScalarValue.ofInt(bd.intValueExact());
                } catch (ArithmeticException ex) {
                    return ScalarValue.ofDouble(bd.doubleValue());
                }
            }

            double d = n.doubleValue();
            if (d == Math.rint(d) && d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                return ScalarValue.ofInt((int) d);
            }
            return ScalarValue.ofDouble(d);
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
            if (value == null) return MissingValue.INSTANCE;
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

        private final Map<String, String> environment = new LinkedHashMap<>();
        private final Map<String, Object> globals = new LinkedHashMap<>();

        public Builder environment(Map<String, String> values) {
            this.environment.clear();
            this.environment.putAll(values);
            return this;
        }

        public Builder env(String key, String value) {
            this.environment.put(key, value);
            return this;
        }

        public Builder globals(Map<String, ?> values) {
            this.globals.clear();
            this.globals.putAll(values);
            return this;
        }

        public Builder global(String key, Object value) {
            this.globals.put(key, value);
            return this;
        }

        public GroovyDeserializer build() {
            return new GroovyDeserializer(environment, globals);
        }
    }
}
