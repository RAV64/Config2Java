package org.msuo.data2java;

interface DataValue {
    String typename();

    default boolean isMissing() {
        return false;
    }

    default boolean isNil() {
        return false;
    }

    default boolean isTable() {
        return false;
    }

    default boolean canBeReadAsEmptyObject() {
        return false;
    }

    default DataTable asTable() {
        throw new IllegalStateException("value is not a table");
    }

    default ScalarValue asScalar() {
        return null;
    }
}
