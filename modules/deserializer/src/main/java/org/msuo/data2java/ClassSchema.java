package org.msuo.data2java;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ClassSchema {

    final List<FieldBinding> bindings;
    final Set<String> keys;

    private ClassSchema(List<FieldBinding> bindings, Set<String> keys) {
        this.bindings = bindings;
        this.keys = keys;
    }

    static ClassSchema build(Type targetType, Class<?> targetRawClass) {
        List<Field> fields = allInstanceFields(targetRawClass);
        List<FieldBinding> bs = new ArrayList<>(fields.size());
        Set<String> keys = new HashSet<>(fields.size());

        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            String key = f.getName();
            Type t = GenericTypeResolver.resolve(
                targetType,
                targetRawClass,
                f.getGenericType()
            );
            TypeAdapter adapter = ObjectMapper.adapterFor(t);

            bs.add(new FieldBinding(f, key, adapter));
            keys.add(key);
        }

        return new ClassSchema(
            Collections.unmodifiableList(bs),
            Collections.unmodifiableSet(keys)
        );
    }

    private static List<Field> allInstanceFields(Class<?> cls) {
        List<Field> out = new ArrayList<>();
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            Field[] fs = c.getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                Field f = fs[i];
                if (Modifier.isStatic(f.getModifiers())) continue;
                out.add(f);
            }
            c = c.getSuperclass();
        }
        return out;
    }
}
