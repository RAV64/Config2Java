package org.msuo.config2java;

interface ConfigErrorType {
    ConfigErrorKind kind();

    String message();
}
