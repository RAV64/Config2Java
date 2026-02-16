package org.msuo.config2java;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class Errors {

    private Errors() {}

    static ConfigErrorType unsupportedType(Type t) {
        return new ConfigErrorTypes.UnsupportedType(t);
    }

    static ConfigErrorType unsupportedParameterized(ParameterizedType pt) {
        return new ConfigErrorTypes.UnsupportedParameterized(pt);
    }

    static ConfigErrorType unsupportedParameterizedRaw(Type raw) {
        return new ConfigErrorTypes.UnsupportedParameterizedRaw(raw);
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

    static ConfigErrorType optionalInnerMustBeConcrete(Type inner) {
        return new ConfigErrorTypes.OptionalInnerMustBeConcrete(inner);
    }

    static ConfigErrorType collectionElementMustBeConcrete(Type elem) {
        return new ConfigErrorTypes.CollectionElementMustBeConcrete(elem);
    }

    static ConfigErrorType mapKeyMustBeConcrete(Type kType) {
        return new ConfigErrorTypes.MapKeyMustBeConcrete(kType);
    }

    static ConfigErrorType mapValueMustBeConcrete(Type vType) {
        return new ConfigErrorTypes.MapValueMustBeConcrete(vType);
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
