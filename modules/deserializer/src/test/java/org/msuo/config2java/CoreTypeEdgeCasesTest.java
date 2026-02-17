package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import org.junit.jupiter.api.Test;

public class CoreTypeEdgeCasesTest {

    static final class GenericHolder<T> {}

    @Test
    void unsupportedParameterizedRaw_reportsTypedError() {
        Type rawTypeVariable = GenericHolder.class.getTypeParameters()[0];
        ParameterizedType weird = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { String.class };
            }

            @Override
            public Type getRawType() {
                return rawTypeVariable;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };

        ErrorCollector errors = new ErrorCollector();
        ReadResult rr = ObjectMapper
            .adapterFor(weird)
            .read(Path.root(), new JavaScalarConfigValue("x"), errors);

        assertFalse(rr.ok);
        assertEquals(1, errors.asList().size());
        assertEquals(
            ConfigErrorTypes.UnsupportedParameterizedRaw.class,
            errors.asList().get(0).getErrorType().getClass()
        );
    }

    @Test
    void unsupportedType_fromTypeVariable_reportsTypedError() {
        Type typeVariable = GenericHolder.class.getTypeParameters()[0];

        ErrorCollector errors = new ErrorCollector();
        ReadResult rr = ObjectMapper
            .adapterFor(typeVariable)
            .read(Path.root(), new JavaScalarConfigValue("x"), errors);

        assertFalse(rr.ok);
        assertEquals(1, errors.asList().size());
        assertEquals(
            ConfigErrorTypes.UnresolvedTypeVariable.class,
            errors.asList().get(0).getErrorType().getClass()
        );
    }

    @Test
    void wildcardType_reportsTypedError() throws NoSuchFieldException {
        Type wildcardType = wildcardFieldArgument(WildcardHolder.class, "values");

        ErrorCollector errors = new ErrorCollector();
        ReadResult rr = ObjectMapper
            .adapterFor(wildcardType)
            .read(Path.root(), new JavaScalarConfigValue("x"), errors);

        assertFalse(rr.ok);
        assertEquals(1, errors.asList().size());
        assertEquals(
            ConfigErrorTypes.WildcardTypeNotSupported.class,
            errors.asList().get(0).getErrorType().getClass()
        );
    }

    static final class WildcardHolder {
        public java.util.List<? extends String> values;
    }

    interface LocalService {}

    static final class LocalServiceImpl implements LocalService {}

    static final class LocalOther {}

    static class LocalBase {}

    static final class LocalDerived extends LocalBase {}

    static final class ClassRefHolder {
        public Class<LocalService> impl;
    }

    static final class ClassRefSuperclassHolder {
        public Class<LocalBase> impl;
    }

    static final class GenericClassRefHolder<U> {
        public Class<U> impl;
    }

    abstract static class AbstractLeaf {
        AbstractLeaf(String value) {}
    }

    static final class AbstractLeafHolder {
        public AbstractLeaf value;
    }

    static final class ThrowingNoArg {
        ThrowingNoArg() {
            throw new IllegalStateException("boom");
        }
    }

    static final class ThrowingNoArgHolder {
        public ThrowingNoArg bad;
    }

    abstract static class AbstractNoArg {
        AbstractNoArg() {}
    }

    static final class AbstractNoArgHolder {
        public AbstractNoArg bad;
    }

    @Test
    void classReferenceField_resolvesAssignableClass() {
        ClassRefHolder cfg = ObjectMapper.deserialize(
            wrap(obj("impl", LocalServiceImpl.class.getName())),
            ClassRefHolder.class
        );
        assertEquals(LocalServiceImpl.class, cfg.impl);
    }

    @Test
    void classReferenceField_rejectsUnknownClass() {
        ConfigDeserializationException ex = assertThrows(
            ConfigDeserializationException.class,
            () -> ObjectMapper.deserialize(
                wrap(obj("impl", "no.such.Type")),
                ClassRefHolder.class
            )
        );
        assertEquals(1, ex.getErrors().size());
        assertEquals(
            ConfigErrorTypes.ClassRefNotFound.class,
            ex.getErrors().get(0).getErrorType().getClass()
        );
        assertEquals(
            java.util.Arrays.asList("impl"),
            ex.getErrors().get(0).getPathSegments()
        );
    }

    @Test
    void classReferenceField_rejectsNonAssignableClass() {
        ConfigDeserializationException ex = assertThrows(
            ConfigDeserializationException.class,
            () -> ObjectMapper.deserialize(
                wrap(obj("impl", LocalOther.class.getName())),
                ClassRefHolder.class
            )
        );
        assertEquals(1, ex.getErrors().size());
        assertEquals(
            ConfigErrorTypes.ClassRefNotAssignable.class,
            ex.getErrors().get(0).getErrorType().getClass()
        );
        assertEquals(
            java.util.Arrays.asList("impl"),
            ex.getErrors().get(0).getPathSegments()
        );
    }

    @Test
    void classReferenceField_acceptsSubclassForSuperclassField() {
        ClassRefSuperclassHolder cfg = ObjectMapper.deserialize(
            wrap(obj("impl", LocalDerived.class.getName())),
            ClassRefSuperclassHolder.class
        );
        assertEquals(LocalDerived.class, cfg.impl);
    }

    @Test
    void classReferenceField_withUnresolvedTypeVariable_reportsUnsupportedType() {
        ConfigDeserializationException ex = assertThrows(
            ConfigDeserializationException.class,
            () -> ObjectMapper.deserialize(
                wrap(obj("impl", LocalDerived.class.getName())),
                GenericClassRefHolder.class
            )
        );
        assertEquals(
            ConfigErrorTypes.UnsupportedType.class,
            ex.getErrors().get(0).getErrorType().getClass()
        );
        assertEquals(java.util.Arrays.asList("impl"), ex.getErrors().get(0).getPathSegments());
    }

    @Test
    void leafCtorInstantiationFailure_reportsCtorCallFailed() {
        ConfigDeserializationException ex = assertThrows(
            ConfigDeserializationException.class,
            () -> ObjectMapper.deserialize(
                wrap(obj("value", "x")),
                AbstractLeafHolder.class
            )
        );
        assertEquals(
            ConfigErrorTypes.CtorCallFailed.class,
            ex.getErrors().get(0).getErrorType().getClass()
        );
        assertEquals(java.util.Arrays.asList("value"), ex.getErrors().get(0).getPathSegments());
    }

    @Test
    void nestedNoArgConstructorThrowing_reportsCtorFailed() {
        ConfigDeserializationException ex = assertThrows(
            ConfigDeserializationException.class,
            () -> ObjectMapper.deserialize(
                wrap(obj("bad", obj())),
                ThrowingNoArgHolder.class
            )
        );
        assertEquals(
            ConfigErrorTypes.CtorFailed.class,
            ex.getErrors().get(0).getErrorType().getClass()
        );
        assertEquals(java.util.Arrays.asList("bad"), ex.getErrors().get(0).getPathSegments());
    }

    @Test
    void nestedAbstractNoArgType_reportsInstantiateFailed() {
        ConfigDeserializationException ex = assertThrows(
            ConfigDeserializationException.class,
            () -> ObjectMapper.deserialize(
                wrap(obj("bad", obj())),
                AbstractNoArgHolder.class
            )
        );
        assertEquals(
            ConfigErrorTypes.InstantiateFailed.class,
            ex.getErrors().get(0).getErrorType().getClass()
        );
        assertEquals(java.util.Arrays.asList("bad"), ex.getErrors().get(0).getPathSegments());
    }

    @Test
    void reflectionMapper_fieldWriteAccessBranch_mapsToFieldSetAccess() {
        ConfigErrorType error = ReflectionErrorMapper.fieldWriteError(
            new IllegalAccessException("x")
        );
        assertEquals(ConfigErrorTypes.FieldSetAccess.class, error.getClass());
    }

    @Test
    void reflectionMapper_fieldWriteTypeBranch_mapsToFieldSetTypeMismatch() {
        ConfigErrorType error = ReflectionErrorMapper.fieldWriteError(
            new IllegalArgumentException("x")
        );
        assertEquals(ConfigErrorTypes.FieldSetTypeMismatch.class, error.getClass());
    }

    @Test
    void reflectionMapper_fieldReadBranch_mapsToFieldReadAccess() {
        ConfigErrorType error = ReflectionErrorMapper.fieldReadError(
            new RuntimeException("x")
        );
        assertEquals(ConfigErrorTypes.FieldReadAccess.class, error.getClass());
    }

    @Test
    void reflectionMapper_invocationCtorBranch_mapsToCtorFailed() {
        ConfigErrorType error = ReflectionErrorMapper.instantiateError(
            AbstractNoArg.class,
            new InvocationTargetException(new IllegalStateException("boom"))
        );
        assertEquals(ConfigErrorTypes.CtorFailed.class, error.getClass());
    }

    private static java.util.Map<String, Object> obj(Object... kvs) {
        java.util.LinkedHashMap<String, Object> out = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            out.put((String) kvs[i], kvs[i + 1]);
        }
        return out;
    }

    private static ConfigValue wrap(Object value) {
        return new MapListConfigValue(value) {
            @Override
            protected ConfigValue wrap(Object v) {
                return CoreTypeEdgeCasesTest.wrap(v);
            }
        };
    }

    private static Type wildcardFieldArgument(Class<?> owner, String fieldName)
        throws NoSuchFieldException {
        Field f = owner.getDeclaredField(fieldName);
        ParameterizedType p = (ParameterizedType) f.getGenericType();
        Type arg = p.getActualTypeArguments()[0];
        assertTrue(arg instanceof WildcardType);
        return arg;
    }
}
