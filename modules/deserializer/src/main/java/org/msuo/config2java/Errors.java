package org.msuo.config2java;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

final class Errors {

    private Errors() {}

    static ConfigErrorType unsupportedType(Type t) {
        return new ConfigErrorTypes.UnsupportedType(t);
    }

    static ConfigErrorType unresolvedTypeVariable(TypeVariable<?> tv) {
        return new ConfigErrorTypes.UnresolvedTypeVariable(tv);
    }

    static ConfigErrorType wildcardTypeNotSupported(WildcardType wt) {
        return new ConfigErrorTypes.WildcardTypeNotSupported(wt);
    }

    static ConfigErrorType unsupportedParameterizedRaw(Type raw) {
        return new ConfigErrorTypes.UnsupportedParameterizedRaw(raw);
    }

    static ConfigErrorType classRefExpectedString(ConfigValue got) {
        return new ConfigErrorTypes.ClassRefExpectedString(got.typename());
    }

    static ConfigErrorType classRefNotFound(String className) {
        return new ConfigErrorTypes.ClassRefNotFound(className);
    }

    static ConfigErrorType classRefNotAssignable(
        Class<?> expectedBaseType,
        Class<?> actualType
    ) {
        return new ConfigErrorTypes.ClassRefNotAssignable(expectedBaseType, actualType);
    }

    static ConfigErrorType primitiveNotSupported(Class<?> prim) {
        return new ConfigErrorTypes.PrimitiveNotSupported(prim);
    }

    static ConfigErrorType enumExpectedString(ConfigValue got) {
        return new ConfigErrorTypes.EnumExpectedString(got.typename());
    }

    static ConfigErrorType enumUnknown(Class<?> enumClass, String name) {
        return new ConfigErrorTypes.EnumUnknown(enumClass, name);
    }

    static ConfigErrorType expectedScalar(ConfigValue got) {
        return new ConfigErrorTypes.ExpectedScalar(got.typename());
    }

    static ConfigErrorType mapExpected(ConfigValue got) {
        return new ConfigErrorTypes.MapExpected(got.typename());
    }

    static ConfigErrorType collectionExpected(Class<?> raw, ConfigValue got) {
        return new ConfigErrorTypes.CollectionExpected(raw, got.typename());
    }

    static ConfigErrorType missingRequiredField() {
        return new ConfigErrorTypes.MissingRequiredField();
    }

    static ConfigErrorType noOneArgCtor(Class<?> target, Class<?> argType) {
        return new ConfigErrorTypes.NoOneArgCtor(target, argType);
    }

    static ConfigErrorType ctorRejected(Class<?> target, Throwable cause, Object value) {
        return new ConfigErrorTypes.CtorRejected(target, cause, value);
    }

    static ConfigErrorType ctorCallFailed(Class<?> target, Exception e) {
        return new ConfigErrorTypes.CtorCallFailed(target, e);
    }

    static ConfigErrorType noNoArgCtor(Class<?> cls) {
        return new ConfigErrorTypes.NoNoArgCtor(cls);
    }

    static ConfigErrorType ctorFailed(Class<?> cls, Throwable cause) {
        return new ConfigErrorTypes.CtorFailed(cls, cause);
    }

    static ConfigErrorType instantiateFailed(Class<?> cls, Exception e) {
        return new ConfigErrorTypes.InstantiateFailed(cls, e);
    }

    static ConfigErrorType fieldSetAccess(Exception e) {
        return new ConfigErrorTypes.FieldSetAccess(e);
    }

    static ConfigErrorType fieldSetTypeMismatch(Exception e) {
        return new ConfigErrorTypes.FieldSetTypeMismatch(e);
    }

    static ConfigErrorType fieldAccessSetup(Exception e) {
        return new ConfigErrorTypes.FieldAccessSetup(e);
    }

    static ConfigErrorType fieldReadAccess(Exception e) {
        return new ConfigErrorTypes.FieldReadAccess(e);
    }
}
