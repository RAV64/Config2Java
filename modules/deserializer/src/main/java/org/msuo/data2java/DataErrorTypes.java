package org.msuo.data2java;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class DataErrorTypes {

    private DataErrorTypes() {}

    public static final class UnsupportedType implements DataErrorType {
        private final Type type;
        UnsupportedType(Type type) { this.type = type; }
        @Override public String message() { return "Unsupported Type: " + type; }
    }

    public static final class UnresolvedTypeVariable implements DataErrorType {
        private final TypeVariable<?> typeVariable;
        UnresolvedTypeVariable(TypeVariable<?> typeVariable) {
            this.typeVariable = typeVariable;
        }
        @Override
        public String message() {
            return "Unresolved generic type variable: " + typeVariable.getName();
        }
    }

    public static final class WildcardTypeNotSupported implements DataErrorType {
        private final WildcardType wildcardType;
        WildcardTypeNotSupported(WildcardType wildcardType) {
            this.wildcardType = wildcardType;
        }
        @Override
        public String message() {
            return "Wildcard generic types are not supported here: " + wildcardType;
        }
    }

    public static final class UnsupportedParameterizedRaw implements DataErrorType {
        private final Type raw;
        UnsupportedParameterizedRaw(Type raw) { this.raw = raw; }
        @Override public String message() { return "Unsupported parameterized raw type: " + raw; }
    }

    public static final class ClassRefExpectedString implements DataErrorType {
        private final String gotTypeName;
        ClassRefExpectedString(String gotTypeName) {
            this.gotTypeName = gotTypeName;
        }
        @Override
        public String message() {
            return "Class reference expects string class name, got: " + gotTypeName;
        }
    }

    public static final class ClassRefNotFound implements DataErrorType {
        private final String className;
        ClassRefNotFound(String className) {
            this.className = className;
        }
        @Override
        public String message() {
            return "Class not found: " + className;
        }
    }

    public static final class ClassRefNotAssignable implements DataErrorType {
        private final Class<?> expectedBaseType;
        private final Class<?> actualType;
        ClassRefNotAssignable(Class<?> expectedBaseType, Class<?> actualType) {
            this.expectedBaseType = expectedBaseType;
            this.actualType = actualType;
        }
        @Override
        public String message() {
            return "Class " + actualType.getName() + " is not assignable to " + expectedBaseType.getName();
        }
    }

    public static final class PrimitiveNotSupported implements DataErrorType {
        private final Class<?> primitive;
        PrimitiveNotSupported(Class<?> primitive) { this.primitive = primitive; }
        @Override public String message() { return "Primitive field types are not supported: " + primitive.getName(); }
    }

    public static final class EnumExpectedString implements DataErrorType {
        private final String gotTypeName;
        EnumExpectedString(String gotTypeName) { this.gotTypeName = gotTypeName; }
        @Override public String message() { return "Enum expects string name, got: " + gotTypeName; }
    }

    public static final class EnumUnknown implements DataErrorType {
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

    public static final class ExpectedScalar implements DataErrorType {
        private final String gotTypeName;
        ExpectedScalar(String gotTypeName) { this.gotTypeName = gotTypeName; }
        @Override public String message() { return "Expected primitive (string/number/bool), got: " + gotTypeName; }
    }

    public static final class MapExpected implements DataErrorType {
        private final String gotTypeName;
        MapExpected(String gotTypeName) { this.gotTypeName = gotTypeName; }
        @Override public String message() { return "Expected table for Map, got: " + gotTypeName; }
    }

    public static final class CollectionExpected implements DataErrorType {
        private final Class<?> raw;
        private final String gotTypeName;
        CollectionExpected(Class<?> raw, String gotTypeName) {
            this.raw = raw;
            this.gotTypeName = gotTypeName;
        }
        @Override public String message() { return "Expected table/array for " + raw.getSimpleName() + ", got: " + gotTypeName; }
    }

    public static final class MissingRequiredField implements DataErrorType {
        @Override public String message() { return "Missing required field (no default value)."; }
    }

    public static final class NoOneArgCtor implements DataErrorType {
        private final Class<?> target;
        private final Class<?> argType;
        NoOneArgCtor(Class<?> target, Class<?> argType) {
            this.target = target;
            this.argType = argType;
        }
        @Override public String message() { return "No 1-arg constructor on " + target.getName() + " accepting " + argType.getName(); }
    }

    public static final class CtorRejected implements DataErrorType {
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

    public static final class CtorCallFailed implements DataErrorType {
        private final Class<?> target;
        private final Exception error;
        CtorCallFailed(Class<?> target, Exception error) {
            this.target = target;
            this.error = error;
        }
        @Override public String message() { return "Failed calling constructor for " + target.getName() + ": " + error.getMessage(); }
    }

    public static final class NoNoArgCtor implements DataErrorType {
        private final Class<?> cls;
        NoNoArgCtor(Class<?> cls) { this.cls = cls; }
        @Override public String message() { return "No no-arg constructor for nested object type: " + cls.getName(); }
    }

    public static final class CtorFailed implements DataErrorType {
        private final Class<?> cls;
        private final Throwable cause;
        CtorFailed(Class<?> cls, Throwable cause) {
            this.cls = cls;
            this.cause = cause;
        }
        @Override public String message() { return "Constructor failed for " + cls.getName() + ": " + cause.getMessage(); }
    }

    public static final class InstantiateFailed implements DataErrorType {
        private final Class<?> cls;
        private final Exception error;
        InstantiateFailed(Class<?> cls, Exception error) {
            this.cls = cls;
            this.error = error;
        }
        @Override public String message() { return "Failed to instantiate " + cls.getName() + ": " + error.getMessage(); }
    }

    public static final class FieldSetAccess implements DataErrorType {
        private final Exception error;
        FieldSetAccess(Exception error) { this.error = error; }
        @Override public String message() { return "Failed to set field (access): " + error.getMessage(); }
    }

    public static final class FieldSetTypeMismatch implements DataErrorType {
        private final Exception error;
        FieldSetTypeMismatch(Exception error) { this.error = error; }
        @Override public String message() { return "Failed to set field (type mismatch): " + error.getMessage(); }
    }

    public static final class FieldAccessSetup implements DataErrorType {
        private final Exception error;
        FieldAccessSetup(Exception error) { this.error = error; }
        @Override public String message() { return "Failed to access field reflectively: " + error.getMessage(); }
    }

    public static final class FieldReadAccess implements DataErrorType {
        private final Exception error;
        FieldReadAccess(Exception error) { this.error = error; }
        @Override public String message() { return "Failed to read field default value: " + error.getMessage(); }
    }

}
