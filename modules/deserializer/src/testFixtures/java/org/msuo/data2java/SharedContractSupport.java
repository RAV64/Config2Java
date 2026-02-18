package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public abstract class SharedContractSupport {

    protected abstract <T> T deserialize(String source, Class<T> cls);

    protected final <T> T ok(String source, Class<T> cls) {
        return deserialize(source, cls);
    }

    protected final DataDeserializationException fails(
        String source,
        Class<?> cls
    ) {
        return assertThrows(DataDeserializationException.class, () ->
            deserialize(source, cls)
        );
    }

    protected final void assertSingleError(
        DataDeserializationException ex,
        Class<? extends DataErrorType> errorType,
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

    protected final void assertErrorType(
        DataDeserializationException ex,
        int index,
        Class<? extends DataErrorType> errorType
    ) {
        assertEquals(
            errorType,
            ex.getErrors().get(index).getErrorType().getClass(),
            "Unexpected error type at index " + index
        );
    }

    protected final void assertPathSegments(
        DataDeserializationException ex,
        int index,
        String... expectedPathSegments
    ) {
        List<String> actual = ex.getErrors().get(index).getPathSegments();
        assertEquals(Arrays.asList(expectedPathSegments), actual);
    }

    protected final void assertErrorTreeRootChildren(
        DataDeserializationException ex,
        String... expectedSegments
    ) {
        DataDeserializationException.PathNode root = ex.getErrorPathTree();
        assertEquals("$", root.getSegment());

        Set<String> actual = new HashSet<>();
        for (DataDeserializationException.PathNode child : root.getChildren()) {
            actual.add(child.getSegment());
        }

        for (String segment : expectedSegments) {
            assertTrue(
                actual.contains(segment),
                "Missing expected root tree segment: " + segment
            );
        }
    }

    public static final class NonEmptyString {

        public final String value;

        public NonEmptyString(String s) {
            if (s == null || s.trim().isBlank()) {
                throw new IllegalArgumentException("must be non-empty");
            }
            this.value = s;
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NonEmptyString)) return false;
            NonEmptyString other = (NonEmptyString) o;
            return Objects.equals(this.value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.value);
        }
    }

    public static final class PositiveInteger {

        public final Integer value;

        public PositiveInteger(Integer i) {
            if (i == null || i <= 0) {
                throw new IllegalArgumentException("must be > 0");
            }
            this.value = i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof PositiveInteger) {
                PositiveInteger other = (PositiveInteger) o;
                return Objects.equals(this.value, other.value);
            }
            if (o instanceof Integer) {
                return Objects.equals(this.value, (Integer) o);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }
    }

    public static final class PositiveDouble {

        public final Double value;

        public PositiveDouble(Double d) {
            if (d == null || d <= 0.0) {
                throw new IllegalArgumentException("must be > 0");
            }
            this.value = d;
        }
    }

    public enum Mode {
        DEV,
        PROD,
    }

    static final class NestedPort {
        public PositiveInteger port;
    }

    static final class NestedDefaultsOrOptional {
        public NonEmptyString host = new NonEmptyString("localhost");
        public Optional<NonEmptyString> user = Optional.empty();
    }

    static final class NestedHost {
        public NonEmptyString host;
    }

    static final class ItemName {
        public NonEmptyString name;
    }

    static final class ItemN {
        public PositiveInteger n;
    }

    static final class StringLeaf { public NonEmptyString name; }
    static final class IntLeaf { public PositiveInteger n; }
    static final class DoubleLeaf { public PositiveDouble x; }
    static final class Enum { public Mode mode; }
    static final class BooleanLeaf { public Boolean enabled; }
    static final class Injected { public Mode mode; public NonEmptyString name; }

    static final class MissingRequired { public NonEmptyString name; }
    static final class MissingOptional { public Optional<NonEmptyString> name; }
    static final class OptionalHasDefaultPresent {
        public Optional<NonEmptyString> name = Optional.of(new NonEmptyString("x"));
    }
    static final class DefaultValue { public NonEmptyString name = new NonEmptyString("default"); }
    static final class ExtraKeysIgnored { public NonEmptyString name; }
    static final class DefaultNestedObjectKept {
        public NestedDefaultsOrOptional db = new NestedDefaultsOrOptional();
    }

    static final class NestedPortContainer { public NestedPort db; }
    static final class NestedDefaultsOrOptionalContainer { public NestedDefaultsOrOptional db; }
    static final class EmptyTableButNestedHasRequiredField { public NestedPort db; }

    public static final class NoNoArgNested {
        public NonEmptyString x;
        public NoNoArgNested(NonEmptyString x) { this.x = x; }
    }
    static final class BadNestedNoNoArg { public NoNoArgNested bad; }
    static final class NestedProvidedAsString { public NestedPort db; }

    static final class OptionalOfComplex { public Optional<NestedHost> db; }
    static final class OptionalLeafBadValue { public Optional<PositiveInteger> n; }

    static final class ListOfLeaf { public List<NonEmptyString> tags; }
    static final class SetOfLeaf { public Set<NonEmptyString> tags; }
    static final class MapOfLeaf { public Map<NonEmptyString, PositiveInteger> limits; }
    static final class MapKeyWrongType { public Map<PositiveInteger, PositiveInteger> limits; }
    static final class ListOfComplex { public List<ItemName> items; }
    static final class MapOfComplex { public Map<NonEmptyString, ItemN> items; }
    static final class MissingRequiredList { public List<NonEmptyString> tags; }
    static final class MissingRequiredMap { public Map<NonEmptyString, PositiveInteger> limits; }
    static final class DefaultListKept {
        public List<NonEmptyString> tags = new ArrayList<>(Arrays.asList(new NonEmptyString("d")));
    }
    static final class DefaultMapKept {
        public Map<NonEmptyString, PositiveInteger> limits = new LinkedHashMap<>();
        public DefaultMapKept() { limits.put(new NonEmptyString("x"), new PositiveInteger(1)); }
    }
    static final class ListElementWrongType { public List<NonEmptyString> tags; }
    static final class MapHasBadEntry { public Map<NonEmptyString, PositiveInteger> limits; }
    static final class NestedGenericsMapValue {
        public Map<NonEmptyString, List<NonEmptyString>> bad;
    }

    static final class GenericBox<T> {
        public T value;
    }

    static final class GenericItem<T> {
        public T payload;
    }

    static final class StringConstructedGenericKey<T> {
        public final String value;

        public StringConstructedGenericKey(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StringConstructedGenericKey)) return false;
            StringConstructedGenericKey<?> other = (StringConstructedGenericKey<?>) o;
            return Objects.equals(this.value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.value);
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

    static class UnresolvedTypeVariableField<T> {
        public T value;
    }

    static class UnresolvedTypeVariableArrayField<T> {
        public T[] values;
    }

    static final class WildcardGenericNestedField {
        public GenericBox<? extends NonEmptyString> foo;
    }

    interface ClassRefService {}

    public static final class ClassRefServiceImpl implements ClassRefService {}

    public static final class ClassRefOther {}

    static final class ClassReferenceField {
        public Class<ClassRefService> impl;
    }

    public static class ClassRefBase {}

    public static final class ClassRefDerived extends ClassRefBase {}

    static final class ClassReferenceSuperclassField {
        public Class<ClassRefBase> impl;
    }

    static final class CollectAllErrors {
        public NonEmptyString a;
        public PositiveInteger b;
    }

    static class BaseData { public NonEmptyString base; }
    static final class DerivedData extends BaseData { public PositiveInteger child; }
    static final class PrimitiveFieldNotSupported { public int n; }
    static final class RootIsComplex { public NonEmptyString name; }

    static final class RootA { public NonEmptyString x; }
    static final class RootB { public PositiveInteger y; }

    static class TestMe {
        NonEmptyString x;
        List<PositiveInteger> ints = List.of();
        InnerTest innie;
        Optional<OInnerTest> onnie;

        static class InnerTest {
            String a = "a";
            Integer b = 3;
        }

        static class OInnerTest {
            String c = "c";
            Integer d = 5;
        }
    }

    static final class OptionalLeafNoDefault { public Optional<PositiveInteger> n; }
    static final class OptionalLeafWithDefaultPresent {
        public Optional<NonEmptyString> name = Optional.of(new NonEmptyString("default"));
    }
    static final class OptionalLeafWithDefaultEmpty {
        public Optional<NonEmptyString> name = Optional.empty();
    }
    static final class OptionalComplexWithDefaultPresent {
        public Optional<TestMe.OInnerTest> onnie = Optional.of(new TestMe.OInnerTest());
    }
    static final class OptionalComplexNoDefault { public Optional<TestMe.OInnerTest> onnie; }
    static final class OptionalComplexInnerFieldMutation {
        public Optional<TestMe.OInnerTest> onnie = Optional.of(new TestMe.OInnerTest());
    }
    public static final class NoNoArgNested2 {
        public NonEmptyString x;
        public NoNoArgNested2(NonEmptyString x) { this.x = x; }
    }
    static final class OptionalBadInnerNoNoArg { public Optional<NoNoArgNested2> bad; }
}
