package org.msuo.config2java;

interface ConfigErrorType {
    String message();

    default String type() {
        return getClass().getSimpleName();
    }
}
