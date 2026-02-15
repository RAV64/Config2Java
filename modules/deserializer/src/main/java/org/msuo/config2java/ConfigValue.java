package org.msuo.config2java;

interface ConfigValue {
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

    default ConfigTable asTable() {
        throw new IllegalStateException("value is not a table");
    }

    default ScalarValue asScalar() {
        return null;
    }
}
