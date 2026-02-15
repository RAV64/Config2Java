package org.msuo.config2java;

import java.lang.reflect.Type;

final class TypeUtils {

    static Class<?> requireConcreteClassArg(
        Type t,
        Path path,
        ConfigErrorType error,
        ErrorCollector errors
    ) {
        if (t instanceof Class<?>) return (Class<?>) t;
        errors.add(path, error);
        return null;
    }
}
