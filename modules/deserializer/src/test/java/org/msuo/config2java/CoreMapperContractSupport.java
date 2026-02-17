package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

abstract class CoreMapperContractSupport {

    protected final <T> T ok(Object root, Class<T> cls) {
        return ObjectMapper.deserialize(wrap(root), cls);
    }

    protected final ConfigDeserializationException fails(Object root, Class<?> cls) {
        return assertThrows(
            ConfigDeserializationException.class,
            () -> ObjectMapper.deserialize(wrap(root), cls)
        );
    }

    protected final ConfigValue wrap(Object value) {
        return new MapListConfigValue(value) {
            @Override
            protected ConfigValue wrap(Object v) {
                return CoreMapperContractSupport.this.wrap(v);
            }
        };
    }

    protected static Map<String, Object> obj(Object... kvs) {
        LinkedHashMap<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            out.put((String) kvs[i], kvs[i + 1]);
        }
        return out;
    }

    protected static List<Object> arr(Object... values) {
        return new ArrayList<>(Arrays.asList(values));
    }

    protected final void assertSingleError(
        ConfigDeserializationException ex,
        Class<? extends ConfigErrorType> errorType,
        String... expectedPathSegments
    ) {
        assertEquals(1, ex.getErrors().size(), "Expected exactly 1 error");
        assertEquals(
            errorType,
            ex.getErrors().get(0).getErrorType().getClass(),
            "Unexpected error type"
        );
        assertPathSegments(ex, 0, expectedPathSegments);
    }

    protected final void assertPathSegments(
        ConfigDeserializationException ex,
        int index,
        String... expectedPathSegments
    ) {
        List<String> actual = ex.getErrors().get(index).getPathSegments();
        assertEquals(Arrays.asList(expectedPathSegments), actual);
    }

    public static final class NonEmptyString {
        public final String value;

        public NonEmptyString(String value) {
            if (value == null || value.trim().isBlank()) {
                throw new IllegalArgumentException("must be non-empty");
            }
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NonEmptyString)) return false;
            return Objects.equals(value, ((NonEmptyString) obj).value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static final class PositiveInteger {
        public final Integer value;

        public PositiveInteger(Integer value) {
            if (value == null || value <= 0) {
                throw new IllegalArgumentException("must be > 0");
            }
            this.value = value;
        }
    }

    public enum Mode {
        DEV,
        PROD,
    }

    static final class StringLeaf { public NonEmptyString name; }
    static final class IntLeaf { public PositiveInteger n; }
    static final class EnumLeaf { public Mode mode; }
    static final class PrimitiveField { public int n; }

    static final class MissingRequired { public NonEmptyString name; }
    static final class MissingOptional { public Optional<NonEmptyString> name; }
    static final class DefaultValue {
        public NonEmptyString name = new NonEmptyString("default");
    }

    static final class NestedPort { public PositiveInteger port; }
    static final class NestedPortContainer { public NestedPort db; }
    static final class BadNestedNoNoArgContainer { public NoNoArgNested bad; }

    public static final class NoNoArgNested {
        public NonEmptyString x;
        public NoNoArgNested(NonEmptyString x) { this.x = x; }
    }

    static final class OptionalLeaf { public Optional<PositiveInteger> n; }
    static final class OptionalLeafWithDefault {
        public Optional<NonEmptyString> name = Optional.of(new NonEmptyString("x"));
    }
    static final class OptionalComplex { public Optional<NestedPort> db; }

    static final class ListOfLeaf { public List<NonEmptyString> tags; }
    static final class SetOfLeaf { public Set<NonEmptyString> tags; }
    static final class MapOfLeaf { public Map<NonEmptyString, PositiveInteger> limits; }
    static final class MapKeyWrongType { public Map<PositiveInteger, PositiveInteger> limits; }

    static final class GenericBox<T> { public T value; }
    static final class GenericItem<T> { public T payload; }
    static final class StringConstructedGenericKey<T> {
        public final String value;
        public StringConstructedGenericKey(String value) { this.value = value; }

        @Override
        public boolean equals(Object o) {
            return o instanceof StringConstructedGenericKey &&
            Objects.equals(value, ((StringConstructedGenericKey<?>) o).value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    static final class NestedGenericObjectGraph {
        public GenericBox<List<GenericItem<String>>> foo;
    }

    static final class NestedGenericKeyedListMap {
        public Map<StringConstructedGenericKey<Integer>, List<String>> values;
    }
}
