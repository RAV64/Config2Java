package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import org.junit.jupiter.api.Test;

public class GenericTypeResolverInternalsTest {

    interface GenericInterface<T> {}

    static final class InterfaceImpl implements GenericInterface<String> {}

    static class GenericBase<T> {
        public T value;
    }

    static class GenericMiddle<U> extends GenericBase<List<U>> {}

    static final class GenericConcrete extends GenericMiddle<Integer> {}

    static class GenericWildcardBase<T> {
        public List<? extends T> values;
    }

    static final class GenericWildcardConcrete
        extends GenericWildcardBase<String> {}

    static class GenericArrayBase<T> {
        public T[] values;
    }

    static final class GenericArrayConcrete extends GenericArrayBase<Long> {}

    @Test
    void resolvesTypeVariable_fromGenericInterfaceImplementation() {
        TypeVariable<?> typeVariable = GenericInterface.class.getTypeParameters()[0];
        Type contextType = InterfaceImpl.class.getGenericInterfaces()[0];

        Type resolved = GenericTypeResolver.resolve(
            contextType,
            InterfaceImpl.class,
            typeVariable
        );

        assertEquals(String.class, resolved);
    }

    @Test
    void resolvesTypeVariable_acrossMultiLevelGenericHierarchy()
        throws NoSuchFieldException {
        Field field = GenericBase.class.getDeclaredField("value");
        Type contextType = GenericConcrete.class.getGenericSuperclass();

        Type resolved = GenericTypeResolver.resolve(
            contextType,
            GenericConcrete.class,
            field.getGenericType()
        );

        assertTrue(resolved instanceof ParameterizedType);
        ParameterizedType p = (ParameterizedType) resolved;
        assertEquals(List.class, p.getRawType());
        assertEquals(Integer.class, p.getActualTypeArguments()[0]);
    }

    @Test
    void resolvesWildcardBounds_insideParameterizedType()
        throws NoSuchFieldException {
        Field field = GenericWildcardBase.class.getDeclaredField("values");
        Type contextType = GenericWildcardConcrete.class.getGenericSuperclass();

        Type resolved = GenericTypeResolver.resolve(
            contextType,
            GenericWildcardConcrete.class,
            field.getGenericType()
        );

        assertTrue(resolved instanceof ParameterizedType);
        Type arg = ((ParameterizedType) resolved).getActualTypeArguments()[0];
        assertTrue(arg instanceof WildcardType);
        WildcardType wildcard = (WildcardType) arg;
        assertEquals(String.class, wildcard.getUpperBounds()[0]);
    }

    @Test
    void resolvesGenericArrayComponentType() throws NoSuchFieldException {
        Field field = GenericArrayBase.class.getDeclaredField("values");
        Type contextType = GenericArrayConcrete.class.getGenericSuperclass();

        Type resolved = GenericTypeResolver.resolve(
            contextType,
            GenericArrayConcrete.class,
            field.getGenericType()
        );

        assertTrue(resolved instanceof GenericArrayType);
        GenericArrayType array = (GenericArrayType) resolved;
        assertEquals(Long.class, array.getGenericComponentType());
    }
}
