package org.msuo.data2java;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

final class Errors {

    private Errors() {}

    static DataErrorType unsupportedType(Type t) {
        return new DataErrorTypes.UnsupportedType(t);
    }

    static DataErrorType unresolvedTypeVariable(TypeVariable<?> tv) {
        return new DataErrorTypes.UnresolvedTypeVariable(tv);
    }

    static DataErrorType wildcardTypeNotSupported(WildcardType wt) {
        return new DataErrorTypes.WildcardTypeNotSupported(wt);
    }

    static DataErrorType unsupportedParameterizedRaw(Type raw) {
        return new DataErrorTypes.UnsupportedParameterizedRaw(raw);
    }

    static DataErrorType classRefExpectedString(DataValue got) {
        return new DataErrorTypes.ClassRefExpectedString(got.typename());
    }

    static DataErrorType classRefNotFound(String className) {
        return new DataErrorTypes.ClassRefNotFound(className);
    }

    static DataErrorType classRefNotAssignable(
        Class<?> expectedBaseType,
        Class<?> actualType
    ) {
        return new DataErrorTypes.ClassRefNotAssignable(expectedBaseType, actualType);
    }

    static DataErrorType primitiveNotSupported(Class<?> prim) {
        return new DataErrorTypes.PrimitiveNotSupported(prim);
    }

    static DataErrorType enumExpectedString(DataValue got) {
        return new DataErrorTypes.EnumExpectedString(got.typename());
    }

    static DataErrorType enumUnknown(Class<?> enumClass, String name) {
        return new DataErrorTypes.EnumUnknown(enumClass, name);
    }

    static DataErrorType expectedScalar(DataValue got) {
        return new DataErrorTypes.ExpectedScalar(got.typename());
    }

    static DataErrorType mapExpected(DataValue got) {
        return new DataErrorTypes.MapExpected(got.typename());
    }

    static DataErrorType collectionExpected(Class<?> raw, DataValue got) {
        return new DataErrorTypes.CollectionExpected(raw, got.typename());
    }

    static DataErrorType missingRequiredField() {
        return new DataErrorTypes.MissingRequiredField();
    }

    static DataErrorType unknownField(Class<?> cls, String fieldName) {
        return new DataErrorTypes.UnknownField(cls, fieldName);
    }

    static DataErrorType noOneArgCtor(Class<?> target, Class<?> argType) {
        return new DataErrorTypes.NoOneArgCtor(target, argType);
    }

    static DataErrorType ctorRejected(Class<?> target, Throwable cause, Object value) {
        return new DataErrorTypes.CtorRejected(target, cause, value);
    }

    static DataErrorType ctorCallFailed(Class<?> target, Exception e) {
        return new DataErrorTypes.CtorCallFailed(target, e);
    }

    static DataErrorType noNoArgCtor(Class<?> cls) {
        return new DataErrorTypes.NoNoArgCtor(cls);
    }

    static DataErrorType ctorFailed(Class<?> cls, Throwable cause) {
        return new DataErrorTypes.CtorFailed(cls, cause);
    }

    static DataErrorType instantiateFailed(Class<?> cls, Exception e) {
        return new DataErrorTypes.InstantiateFailed(cls, e);
    }

    static DataErrorType fieldSetAccess(Exception e) {
        return new DataErrorTypes.FieldSetAccess(e);
    }

    static DataErrorType fieldSetTypeMismatch(Exception e) {
        return new DataErrorTypes.FieldSetTypeMismatch(e);
    }

    static DataErrorType fieldAccessSetup(Exception e) {
        return new DataErrorTypes.FieldAccessSetup(e);
    }

    static DataErrorType fieldReadAccess(Exception e) {
        return new DataErrorTypes.FieldReadAccess(e);
    }
}
