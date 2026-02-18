package org.msuo.data2java;

import java.lang.reflect.InvocationTargetException;

final class ReflectionErrorMapper {

    private ReflectionErrorMapper() {}

    static DataErrorType instantiateError(
        Class<?> cls,
        ReflectiveOperationException error
    ) {
        if (error instanceof InvocationTargetException) {
            Throwable cause = ((InvocationTargetException) error).getCause();
            return Errors.ctorFailed(cls, cause != null ? cause : error);
        }
        return Errors.instantiateFailed(cls, error);
    }

    static DataErrorType leafCtorError(
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

    static DataErrorType fieldReadError(Exception error) {
        return Errors.fieldReadAccess(error);
    }

    static DataErrorType fieldWriteError(Exception error) {
        if (error instanceof IllegalAccessException) {
            return Errors.fieldSetAccess(error);
        }
        return Errors.fieldSetTypeMismatch(error);
    }
}
