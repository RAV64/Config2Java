package org.msuo.config2java;

import java.lang.reflect.Field;

final class FieldBinding {

    final FieldAccess access;
    final String key;
    final TypeAdapter adapter;

    FieldBinding(Field field, String key, TypeAdapter adapter) {
        this.access = new FieldAccess(field);
        this.key = key;
        this.adapter = adapter;
    }
}
