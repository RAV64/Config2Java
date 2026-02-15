package org.msuo.config2java;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

final class ClassAdapter implements TypeAdapter {

    private final Class<?> cls;

    ClassAdapter(Class<?> cls) {
        this.cls = cls;
    }

    @Override
    public ReadResult read(Path path, ConfigValue value, ErrorCollector errors) {
        if (value.isTable()) {
            return ObjectReader.readObject(path, cls, value.asTable(), errors);
        }
        return LeafReader.readLeaf(path, cls, value, errors);
    }

    private static final class ObjectReader {

        private static final ClassValue<ClassSchema> SCHEMA_CACHE =
            new ClassValue<>() {
                @Override
                protected ClassSchema computeValue(Class<?> type) {
                    return ClassSchema.build(type);
                }
            };

        static ReadResult readObject(Path path, Class<?> cls, ConfigTable table, ErrorCollector errors) {
            Object instance = instantiateNoArg(path, cls, errors);
            if (instance == null) return ReadResult.fail();

            ClassSchema schema = SCHEMA_CACHE.get(cls);
            for (int i = 0; i < schema.bindings.size(); i++) {
                bindField(instance, schema.bindings.get(i), table, path, errors);
            }

            return ReadResult.ok(instance);
        }

        private static void bindField(Object instance, FieldBinding b, ConfigTable table, Path basePath, ErrorCollector errors) {
            Path fieldPath = basePath.field(b.key);

            ConfigValue v = table.getField(b.key);
            boolean provided = !v.isMissing();

            Object currentDefault = getFieldValueQuiet(instance, b.field);

            if (!provided) {
                if (currentDefault != null) return;

                ReadResult rr = b.adapter.missing(fieldPath, errors);
                if (rr.ok) {
                    setFieldQuiet(instance, b.field, rr.value, fieldPath, errors);
                }
                return;
            }

            ReadResult rr = b.adapter.read(fieldPath, v, errors);
            if (rr.ok) {
                setFieldQuiet(instance, b.field, rr.value, fieldPath, errors);
            }
        }
    }

    private static Object instantiateNoArg(Path path, Class<?> cls, ErrorCollector errors) {
        try {
            Constructor<?> c = cls.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (NoSuchMethodException e) {
            errors.add(path, Errors.noNoArgCtor(cls));
        } catch (InvocationTargetException e) {
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;
            errors.add(path, Errors.ctorFailed(cls, cause));
        } catch (ReflectiveOperationException e) {
            errors.add(path, Errors.instantiateFailed(cls, e));
        }
        return null;
    }

    private static Object getFieldValueQuiet(Object instance, Field f) {
        try {
            return f.get(instance);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static void setFieldQuiet(Object instance, Field f, Object value, Path path, ErrorCollector errors) {
        try {
            f.set(instance, value);
        } catch (IllegalAccessException e) {
            errors.add(path, Errors.fieldSetAccess(e));
        } catch (IllegalArgumentException e) {
            errors.add(path, Errors.fieldSetTypeMismatch(e));
        }
    }
}
