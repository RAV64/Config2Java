package org.msuo.data2java;

final class JavaScalarDataValue implements DataValue {

    private final Object value;

    JavaScalarDataValue(Object value) {
        this.value = value;
    }

    @Override
    public String typename() {
        return typenameOf(value);
    }

    static String typenameOf(Object value) {
        if (value == null) return "nil";
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
    public ScalarValue asScalar() {
        return scalarOf(value);
    }

    static ScalarValue scalarOf(Object value) {
        if (value == null) return null;
        if (value instanceof CharSequence) return ScalarValue.ofString(value.toString());
        if (value instanceof Boolean) return ScalarValue.ofBoolean((Boolean) value);
        if (value instanceof Number) return ScalarNumbers.fromNumber((Number) value);
        return null;
    }
}
