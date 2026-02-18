package org.msuo.data2java;

final class ScalarValue {

    final Object value;
    final Class<?> boxedType;

    private ScalarValue(Object value, Class<?> boxedType) {
        this.value = value;
        this.boxedType = boxedType;
    }

    static ScalarValue ofString(String s) {
        return new ScalarValue(s, String.class);
    }

    static ScalarValue ofInt(Integer i) {
        return new ScalarValue(i, Integer.class);
    }

    static ScalarValue ofDouble(Double d) {
        return new ScalarValue(d, Double.class);
    }

    static ScalarValue ofBoolean(Boolean b) {
        return new ScalarValue(b, Boolean.class);
    }
}
