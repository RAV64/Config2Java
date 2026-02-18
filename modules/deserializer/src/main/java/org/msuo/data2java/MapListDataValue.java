package org.msuo.data2java;

import java.util.List;
import java.util.Map;

abstract class MapListDataValue implements DataValue {

    private final Object value;

    MapListDataValue(Object value) {
        this.value = value;
    }

    protected abstract DataValue wrap(Object value);

    @Override
    public String typename() {
        if (value instanceof Map || value instanceof List) return "table";
        return JavaScalarDataValue.typenameOf(value);
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
    public DataTable asTable() {
        if (value instanceof Map) {
            return new ObjectMapDataTable((Map<?, ?>) value, this::wrap);
        }
        if (value instanceof List) {
            return new ObjectListDataTable((List<?>) value, this::wrap);
        }
        throw new IllegalStateException("not a table value");
    }

    @Override
    public ScalarValue asScalar() {
        return JavaScalarDataValue.scalarOf(value);
    }
}
