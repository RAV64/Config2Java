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
        if (value instanceof Map || value instanceof List) return "table";
        return JavaScalarConfigValue.typenameOf(value);
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
        return JavaScalarConfigValue.scalarOf(value);
    }
}
