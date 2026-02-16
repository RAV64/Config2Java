package org.msuo.config2java;

import java.util.List;
import java.util.Map;

abstract class MapListConfigValue implements ConfigValue {

    private final Object value;

    MapListConfigValue(Object value) {
        this.value = value;
    }

    protected abstract ConfigValue wrap(Object value);

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
        if (value instanceof Map) {
            return new ObjectMapConfigTable((Map<?, ?>) value, this::wrap);
        }
        if (value instanceof List) {
            return new ObjectListConfigTable((List<?>) value, this::wrap);
        }
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
