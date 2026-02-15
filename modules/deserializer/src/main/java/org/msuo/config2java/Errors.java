package org.msuo.config2java;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class Errors {

    private Errors() {}

    static ConfigErrorType unsupportedType(Type t) {
        return new UnsupportedType(t);
    }

    static ConfigErrorType unsupportedParameterized(ParameterizedType pt) {
        return new UnsupportedParameterized(pt);
    }

    static ConfigErrorType unsupportedParameterizedRaw(Type raw) {
        return new UnsupportedParameterizedRaw(raw);
    }

    static ConfigErrorType primitiveNotSupported(Class<?> prim) {
        return new PrimitiveNotSupported(prim);
    }

    static ConfigErrorType enumExpectedString(ConfigValue got) {
        return new EnumExpectedString(got.typename());
    }

    static ConfigErrorType enumUnknown(Class<?> enumClass, String name) {
        return new EnumUnknown(enumClass, name);
    }

    static ConfigErrorType expectedScalar(ConfigValue got) {
        return new ExpectedScalar(got.typename());
    }

    static ConfigErrorType mapExpected(ConfigValue got) {
        return new MapExpected(got.typename());
    }

    static ConfigErrorType collectionExpected(Class<?> raw, ConfigValue got) {
        return new CollectionExpected(raw, got.typename());
    }

    static ConfigErrorType optionalInnerMustBeConcrete(Type inner) {
        return new OptionalInnerMustBeConcrete(inner);
    }

    static ConfigErrorType collectionElementMustBeConcrete(Type elem) {
        return new CollectionElementMustBeConcrete(elem);
    }

    static ConfigErrorType mapKeyMustBeConcrete(Type kType) {
        return new MapKeyMustBeConcrete(kType);
    }

    static ConfigErrorType mapValueMustBeConcrete(Type vType) {
        return new MapValueMustBeConcrete(vType);
    }

    static ConfigErrorType missingRequiredField() {
        return new MissingRequiredField();
    }

    static ConfigErrorType noOneArgCtor(Class<?> target, Class<?> argType) {
        return new NoOneArgCtor(target, argType);
    }

    static ConfigErrorType ctorRejected(Class<?> target, Throwable cause) {
        return new CtorRejected(target, cause);
    }

    static ConfigErrorType ctorCallFailed(Class<?> target, Exception e) {
        return new CtorCallFailed(target, e);
    }

    static ConfigErrorType noNoArgCtor(Class<?> cls) {
        return new NoNoArgCtor(cls);
    }

    static ConfigErrorType ctorFailed(Class<?> cls, Throwable cause) {
        return new CtorFailed(cls, cause);
    }

    static ConfigErrorType instantiateFailed(Class<?> cls, Exception e) {
        return new InstantiateFailed(cls, e);
    }

    static ConfigErrorType fieldSetAccess(Exception e) {
        return new FieldSetAccess(e);
    }

    static ConfigErrorType fieldSetTypeMismatch(Exception e) {
        return new FieldSetTypeMismatch(e);
    }

    private static final class UnsupportedType implements ConfigErrorType {
        private final Type type;
        private UnsupportedType(Type type) { this.type = type; }
        @Override public String message() { return "Unsupported Type: " + type; }
    }

    private static final class UnsupportedParameterized implements ConfigErrorType {
        private final ParameterizedType type;
        private UnsupportedParameterized(ParameterizedType type) { this.type = type; }
        @Override public String message() { return "Unsupported parameterized type: " + type; }
    }

    private static final class UnsupportedParameterizedRaw implements ConfigErrorType {
        private final Type raw;
        private UnsupportedParameterizedRaw(Type raw) { this.raw = raw; }
        @Override public String message() { return "Unsupported parameterized raw type: " + raw; }
    }

    private static final class PrimitiveNotSupported implements ConfigErrorType {
        private final Class<?> primitive;
        private PrimitiveNotSupported(Class<?> primitive) { this.primitive = primitive; }
        @Override public String message() { return "Primitive field types are not supported: " + primitive.getName(); }
    }

    private static final class EnumExpectedString implements ConfigErrorType {
        private final String gotTypeName;
        private EnumExpectedString(String gotTypeName) { this.gotTypeName = gotTypeName; }
        @Override public String message() { return "Enum expects string name, got: " + gotTypeName; }
    }

    private static final class EnumUnknown implements ConfigErrorType {
        private final Class<?> enumClass;
        private final String name;
        private EnumUnknown(Class<?> enumClass, String name) {
            this.enumClass = enumClass;
            this.name = name;
        }
        @Override public String message() { return "Unknown enum value '" + name + "' for " + enumClass.getName(); }
    }

    private static final class ExpectedScalar implements ConfigErrorType {
        private final String gotTypeName;
        private ExpectedScalar(String gotTypeName) { this.gotTypeName = gotTypeName; }
        @Override public String message() { return "Expected primitive (string/number/bool), got: " + gotTypeName; }
    }

    private static final class MapExpected implements ConfigErrorType {
        private final String gotTypeName;
        private MapExpected(String gotTypeName) { this.gotTypeName = gotTypeName; }
        @Override public String message() { return "Expected table for Map, got: " + gotTypeName; }
    }

    private static final class CollectionExpected implements ConfigErrorType {
        private final Class<?> raw;
        private final String gotTypeName;
        private CollectionExpected(Class<?> raw, String gotTypeName) {
            this.raw = raw;
            this.gotTypeName = gotTypeName;
        }
        @Override public String message() { return "Expected table/array for " + raw.getSimpleName() + ", got: " + gotTypeName; }
    }

    private static final class OptionalInnerMustBeConcrete implements ConfigErrorType {
        private final Type inner;
        private OptionalInnerMustBeConcrete(Type inner) { this.inner = inner; }
        @Override public String message() { return "Optional inner type must be a concrete class (no nested generics). Got: " + inner; }
    }

    private static final class CollectionElementMustBeConcrete implements ConfigErrorType {
        private final Type elem;
        private CollectionElementMustBeConcrete(Type elem) { this.elem = elem; }
        @Override public String message() { return "Collection element type must be a concrete class (no nested generics). Got: " + elem; }
    }

    private static final class MapKeyMustBeConcrete implements ConfigErrorType {
        private final Type keyType;
        private MapKeyMustBeConcrete(Type keyType) { this.keyType = keyType; }
        @Override public String message() { return "Map key type must be a concrete class (no nested generics). Got: " + keyType; }
    }

    private static final class MapValueMustBeConcrete implements ConfigErrorType {
        private final Type valueType;
        private MapValueMustBeConcrete(Type valueType) { this.valueType = valueType; }
        @Override public String message() { return "Map value type must be a concrete class (no nested generics). Got: " + valueType; }
    }

    private static final class MissingRequiredField implements ConfigErrorType {
        @Override public String message() { return "Missing required field (no default value)."; }
    }

    private static final class NoOneArgCtor implements ConfigErrorType {
        private final Class<?> target;
        private final Class<?> argType;
        private NoOneArgCtor(Class<?> target, Class<?> argType) {
            this.target = target;
            this.argType = argType;
        }
        @Override public String message() { return "No 1-arg constructor on " + target.getName() + " accepting " + argType.getName(); }
    }

    private static final class CtorRejected implements ConfigErrorType {
        private final Class<?> target;
        private final Throwable cause;
        private CtorRejected(Class<?> target, Throwable cause) {
            this.target = target;
            this.cause = cause;
        }
        @Override public String message() { return "Value rejected by " + target.getSimpleName() + " constructor: " + cause.getMessage(); }
    }

    private static final class CtorCallFailed implements ConfigErrorType {
        private final Class<?> target;
        private final Exception error;
        private CtorCallFailed(Class<?> target, Exception error) {
            this.target = target;
            this.error = error;
        }
        @Override public String message() { return "Failed calling constructor for " + target.getName() + ": " + error.getMessage(); }
    }

    private static final class NoNoArgCtor implements ConfigErrorType {
        private final Class<?> cls;
        private NoNoArgCtor(Class<?> cls) { this.cls = cls; }
        @Override public String message() { return "No no-arg constructor for nested object type: " + cls.getName(); }
    }

    private static final class CtorFailed implements ConfigErrorType {
        private final Class<?> cls;
        private final Throwable cause;
        private CtorFailed(Class<?> cls, Throwable cause) {
            this.cls = cls;
            this.cause = cause;
        }
        @Override public String message() { return "Constructor failed for " + cls.getName() + ": " + cause.getMessage(); }
    }

    private static final class InstantiateFailed implements ConfigErrorType {
        private final Class<?> cls;
        private final Exception error;
        private InstantiateFailed(Class<?> cls, Exception error) {
            this.cls = cls;
            this.error = error;
        }
        @Override public String message() { return "Failed to instantiate " + cls.getName() + ": " + error.getMessage(); }
    }

    private static final class FieldSetAccess implements ConfigErrorType {
        private final Exception error;
        private FieldSetAccess(Exception error) { this.error = error; }
        @Override public String message() { return "Failed to set field (access): " + error.getMessage(); }
    }

    private static final class FieldSetTypeMismatch implements ConfigErrorType {
        private final Exception error;
        private FieldSetTypeMismatch(Exception error) { this.error = error; }
        @Override public String message() { return "Failed to set field (type mismatch): " + error.getMessage(); }
    }
}
