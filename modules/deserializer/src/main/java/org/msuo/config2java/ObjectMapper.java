package org.msuo.config2java;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

final class ObjectMapper {

    private ObjectMapper() {}

    static <T> T deserialize(ConfigValue root, Class<T> configClass) {
        ErrorCollector errors = new ErrorCollector();
        ReadResult rr = readValue(Path.root(), configClass, root, errors);

        if (errors.hasErrors()) {
            throw new ConfigDeserializationException(errors.asList());
        }

        @SuppressWarnings("unchecked")
        T cast = (T) rr.value;
        return cast;
    }

    static ReadResult readValue(Path path, Type targetType, ConfigValue value, ErrorCollector errors) {
        return adapterFor(targetType).read(path, value, errors);
    }

    static TypeAdapter adapterFor(Type targetType) {
        if (targetType instanceof ParameterizedType) {
            return adapterForParameterized((ParameterizedType) targetType);
        }
        if (targetType instanceof Class<?>) {
            return adapterForClass((Class<?>) targetType);
        }
        return new UnsupportedAdapter(Errors.unsupportedType(targetType));
    }

    private static TypeAdapter adapterForParameterized(ParameterizedType pt) {
        Type raw = pt.getRawType();
        if (!(raw instanceof Class<?>)) {
            return new UnsupportedAdapter(Errors.unsupportedParameterizedRaw(raw));
        }

        Class<?> rawClass = (Class<?>) raw;

        if (rawClass == Optional.class) return new OptionalAdapter(pt);
        if (Map.class.isAssignableFrom(rawClass)) return new MapAdapter(pt);
        if (Collection.class.isAssignableFrom(rawClass)) return new CollectionAdapter(pt, rawClass);

        return new ClassAdapter(pt, rawClass);
    }

    private static TypeAdapter adapterForClass(Class<?> cls) {
        if (cls.isPrimitive()) return new PrimitiveRejectedAdapter(cls);
        if (cls.isEnum()) return new EnumAdapter(cls);
        return new ClassAdapter(cls, cls);
    }
}
