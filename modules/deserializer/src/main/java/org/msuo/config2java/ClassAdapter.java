package org.msuo.config2java;

import java.lang.reflect.Constructor;
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
            if (!b.access.ensureAccessible(fieldPath, errors)) return;

            ConfigValue v = table.getField(b.key);
            boolean provided = !v.isMissing();

            Object currentDefault = b.access.readDefault(instance, fieldPath, errors);
            if (b.access.isReadFailed(currentDefault)) return;

            if (!provided) {
                if (currentDefault != null) return;

                ReadResult rr = b.adapter.missing(fieldPath, errors);
                if (rr.ok) {
                    b.access.write(instance, rr.value, fieldPath, errors);
                }
                return;
            }

            ReadResult rr = b.adapter.read(fieldPath, v, errors);
            if (rr.ok) {
                b.access.write(instance, rr.value, fieldPath, errors);
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

}
