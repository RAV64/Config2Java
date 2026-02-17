package org.msuo.config2java;

import java.lang.reflect.Type;

final class ClassReferenceAdapter implements TypeAdapter {

    private final Type expectedType;

    ClassReferenceAdapter(Type expectedType) {
        this.expectedType = expectedType;
    }

    @Override
    public ReadResult read(Path path, ConfigValue value, ErrorCollector errors) {
        String className = ValueCoerce.stringOrError(
            path,
            value,
            errors,
            Errors::classRefExpectedString
        );
        if (className == null) return ReadResult.fail();

        if (!(expectedType instanceof Class<?>)) {
            errors.add(path, Errors.unsupportedType(expectedType));
            return ReadResult.fail();
        }
        Class<?> expectedBaseType = (Class<?>) expectedType;

        Class<?> resolved;
        try {
            resolved = loadClass(className);
        } catch (ClassNotFoundException e) {
            errors.add(path, Errors.classRefNotFound(className));
            return ReadResult.fail();
        }

        if (!expectedBaseType.isAssignableFrom(resolved)) {
            errors.add(path, Errors.classRefNotAssignable(expectedBaseType, resolved));
            return ReadResult.fail();
        }

        return ReadResult.ok(resolved);
    }

    private static Class<?> loadClass(String className)
        throws ClassNotFoundException {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null) {
            return Class.forName(className, false, contextLoader);
        }
        return Class.forName(className);
    }
}
