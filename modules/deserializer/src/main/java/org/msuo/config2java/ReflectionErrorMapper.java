package org.msuo.config2java;

import java.lang.reflect.InvocationTargetException;

final class ReflectionErrorMapper {

    private ReflectionErrorMapper() {}

    static ConfigErrorType instantiateError(
        Class<?> cls,
        ReflectiveOperationException error
    ) {
        if (error instanceof InvocationTargetException) {
            Throwable cause = ((InvocationTargetException) error).getCause();
            return Errors.ctorFailed(cls, cause != null ? cause : error);
        }
        return Errors.instantiateFailed(cls, error);
    }

    static ConfigErrorType leafCtorError(
        Class<?> target,
        Object value,
        ReflectiveOperationException error
    ) {
        if (error instanceof InvocationTargetException) {
            Throwable cause = ((InvocationTargetException) error).getCause();
            return Errors.ctorRejected(target, cause != null ? cause : error, value);
        }
        return Errors.ctorCallFailed(target, error);
    }

    static ConfigErrorType fieldReadError(Exception error) {
        return Errors.fieldReadAccess(error);
    }

    static ConfigErrorType fieldWriteError(Exception error) {
        if (error instanceof IllegalAccessException) {
            return Errors.fieldSetAccess(error);
        }
        return Errors.fieldSetTypeMismatch(error);
    }
}
