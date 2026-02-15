package org.msuo.config2java;

import java.lang.reflect.Field;

final class FieldAccess {

    private static final Object READ_FAILED = new Object();

    private final Field field;

    FieldAccess(Field field) {
        this.field = field;
    }

    Field field() {
        return field;
    }

    boolean ensureAccessible(Path path, ErrorCollector errors) {
        try {
            field.setAccessible(true);
            return true;
        } catch (RuntimeException e) {
            errors.add(path, Errors.fieldAccessSetup(e));
            return false;
        }
    }

    Object readDefault(Object instance, Path path, ErrorCollector errors) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            errors.add(path, Errors.fieldReadAccess(e));
            return READ_FAILED;
        } catch (RuntimeException e) {
            errors.add(path, Errors.fieldReadAccess(e));
            return READ_FAILED;
        }
    }

    boolean isReadFailed(Object value) {
        return value == READ_FAILED;
    }

    void write(Object instance, Object value, Path path, ErrorCollector errors) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            errors.add(path, Errors.fieldSetAccess(e));
        } catch (IllegalArgumentException e) {
            errors.add(path, Errors.fieldSetTypeMismatch(e));
        }
    }
}
