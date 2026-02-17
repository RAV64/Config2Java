package org.msuo.config2java;

import java.lang.reflect.Constructor;

final class LeafReader {

    private LeafReader() {}

    static ReadResult readLeaf(Path path, Class<?> target, ConfigValue value, ErrorCollector errors) {
        ScalarValue scalar = ValueCoerce.scalarOrError(path, value, errors);
        if (scalar == null) return ReadResult.fail();

        if (target.isAssignableFrom(scalar.boxedType)) {
            return ReadResult.ok(scalar.value);
        }

        Constructor<?> ctor = findOneArgCtor(target, scalar.boxedType);
        if (ctor == null) {
            errors.add(path, Errors.noOneArgCtor(target, scalar.boxedType));
            return ReadResult.fail();
        }

        try {
            ctor.setAccessible(true);
            return ReadResult.ok(ctor.newInstance(scalar.value));
        } catch (ReflectiveOperationException e) {
            errors.add(path, ReflectionErrorMapper.leafCtorError(target, scalar.value, e));
            return ReadResult.fail();
        }
    }

    private static Constructor<?> findOneArgCtor(Class<?> target, Class<?> paramType) {
        Constructor<?>[] ctors = target.getDeclaredConstructors();
        for (int i = 0; i < ctors.length; i++) {
            Constructor<?> c = ctors[i];
            if (c.getParameterCount() != 1) continue;
            if (c.getParameterTypes()[0] == paramType) return c;
        }
        return null;
    }
}
