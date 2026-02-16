package org.msuo.config2java;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class ConfigErrorTypes {

    private ConfigErrorTypes() {}

    public static final class UnsupportedType implements ConfigErrorType {
        private final Type type;
        UnsupportedType(Type type) { this.type = type; }
        @Override public String message() { return "Unsupported Type: " + type; }
    }

    public static final class UnsupportedParameterized implements ConfigErrorType {
        private final ParameterizedType type;
        UnsupportedParameterized(ParameterizedType type) { this.type = type; }
        @Override public String message() { return "Unsupported parameterized type: " + type; }
    }

    public static final class UnsupportedParameterizedRaw implements ConfigErrorType {
        private final Type raw;
        UnsupportedParameterizedRaw(Type raw) { this.raw = raw; }
        @Override public String message() { return "Unsupported parameterized raw type: " + raw; }
    }

    public static final class PrimitiveNotSupported implements ConfigErrorType {
        private final Class<?> primitive;
        PrimitiveNotSupported(Class<?> primitive) { this.primitive = primitive; }
        @Override public String message() { return "Primitive field types are not supported: " + primitive.getName(); }
    }

    public static final class EnumExpectedString implements ConfigErrorType {
        private final String gotTypeName;
        EnumExpectedString(String gotTypeName) { this.gotTypeName = gotTypeName; }
        @Override public String message() { return "Enum expects string name, got: " + gotTypeName; }
    }

    public static final class EnumUnknown implements ConfigErrorType {
        private final Class<?> enumClass;
        private final String name;
        EnumUnknown(Class<?> enumClass, String name) {
            this.enumClass = enumClass;
            this.name = name;
        }
        @Override public String message() {
            Object[] values = enumClass.getEnumConstants();
            String valid = values == null
                ? "[]"
                : Arrays.stream(values).map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
            return "Unknown enum value '" + name + "' for " + enumClass.getName() + ". Valid values: " + valid;
        }
    }

    public static final class ExpectedScalar implements ConfigErrorType {
        private final String gotTypeName;
        ExpectedScalar(String gotTypeName) { this.gotTypeName = gotTypeName; }
        @Override public String message() { return "Expected primitive (string/number/bool), got: " + gotTypeName; }
    }

    public static final class MapExpected implements ConfigErrorType {
        private final String gotTypeName;
        MapExpected(String gotTypeName) { this.gotTypeName = gotTypeName; }
        @Override public String message() { return "Expected table for Map, got: " + gotTypeName; }
    }

    public static final class CollectionExpected implements ConfigErrorType {
        private final Class<?> raw;
        private final String gotTypeName;
        CollectionExpected(Class<?> raw, String gotTypeName) {
            this.raw = raw;
            this.gotTypeName = gotTypeName;
        }
        @Override public String message() { return "Expected table/array for " + raw.getSimpleName() + ", got: " + gotTypeName; }
    }

    public static final class OptionalInnerMustBeConcrete implements ConfigErrorType {
        private final Type inner;
        OptionalInnerMustBeConcrete(Type inner) { this.inner = inner; }
        @Override public String message() { return "Optional inner type must be a concrete class (no nested generics). Got: " + inner; }
    }

    public static final class CollectionElementMustBeConcrete implements ConfigErrorType {
        private final Type elem;
        CollectionElementMustBeConcrete(Type elem) { this.elem = elem; }
        @Override public String message() { return "Collection element type must be a concrete class (no nested generics). Got: " + elem; }
    }

    public static final class MapKeyMustBeConcrete implements ConfigErrorType {
        private final Type keyType;
        MapKeyMustBeConcrete(Type keyType) { this.keyType = keyType; }
        @Override public String message() { return "Map key type must be a concrete class (no nested generics). Got: " + keyType; }
    }

    public static final class MapValueMustBeConcrete implements ConfigErrorType {
        private final Type valueType;
        MapValueMustBeConcrete(Type valueType) { this.valueType = valueType; }
        @Override public String message() { return "Map value type must be a concrete class (no nested generics). Got: " + valueType; }
    }

    public static final class MissingRequiredField implements ConfigErrorType {
        @Override public String message() { return "Missing required field (no default value)."; }
    }

    public static final class NoOneArgCtor implements ConfigErrorType {
        private final Class<?> target;
        private final Class<?> argType;
        NoOneArgCtor(Class<?> target, Class<?> argType) {
            this.target = target;
            this.argType = argType;
        }
        @Override public String message() { return "No 1-arg constructor on " + target.getName() + " accepting " + argType.getName(); }
    }

    public static final class CtorRejected implements ConfigErrorType {
        private final Class<?> target;
        private final Throwable cause;
        private final Object value;

        CtorRejected(Class<?> target, Throwable cause, Object value) {
            this.target = target;
            this.cause = cause;
            this.value = value;
        }
        @Override public String message() {
            return "Value [" + String.valueOf(value) + "] rejected by " + target.getSimpleName() + ": " + cause.getMessage();
        }
    }

    public static final class CtorCallFailed implements ConfigErrorType {
        private final Class<?> target;
        private final Exception error;
        CtorCallFailed(Class<?> target, Exception error) {
            this.target = target;
            this.error = error;
        }
        @Override public String message() { return "Failed calling constructor for " + target.getName() + ": " + error.getMessage(); }
    }

    public static final class NoNoArgCtor implements ConfigErrorType {
        private final Class<?> cls;
        NoNoArgCtor(Class<?> cls) { this.cls = cls; }
        @Override public String message() { return "No no-arg constructor for nested object type: " + cls.getName(); }
    }

    public static final class CtorFailed implements ConfigErrorType {
        private final Class<?> cls;
        private final Throwable cause;
        CtorFailed(Class<?> cls, Throwable cause) {
            this.cls = cls;
            this.cause = cause;
        }
        @Override public String message() { return "Constructor failed for " + cls.getName() + ": " + cause.getMessage(); }
    }

    public static final class InstantiateFailed implements ConfigErrorType {
        private final Class<?> cls;
        private final Exception error;
        InstantiateFailed(Class<?> cls, Exception error) {
            this.cls = cls;
            this.error = error;
        }
        @Override public String message() { return "Failed to instantiate " + cls.getName() + ": " + error.getMessage(); }
    }

    public static final class FieldSetAccess implements ConfigErrorType {
        private final Exception error;
        FieldSetAccess(Exception error) { this.error = error; }
        @Override public String message() { return "Failed to set field (access): " + error.getMessage(); }
    }

    public static final class FieldSetTypeMismatch implements ConfigErrorType {
        private final Exception error;
        FieldSetTypeMismatch(Exception error) { this.error = error; }
        @Override public String message() { return "Failed to set field (type mismatch): " + error.getMessage(); }
    }

    public static final class FieldAccessSetup implements ConfigErrorType {
        private final Exception error;
        FieldAccessSetup(Exception error) { this.error = error; }
        @Override public String message() { return "Failed to access field reflectively: " + error.getMessage(); }
    }

    public static final class FieldReadAccess implements ConfigErrorType {
        private final Exception error;
        FieldReadAccess(Exception error) { this.error = error; }
        @Override public String message() { return "Failed to read field default value: " + error.getMessage(); }
    }

}
