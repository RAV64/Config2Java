package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public abstract class SharedContractSupport {

    protected abstract Deserializer deserializer();

    protected final <T> T ok(String source, Class<T> cls) {
        return deserializer().deserialize(source, cls);
    }

    protected final ConfigDeserializationException fails(
        String source,
        Class<?> cls
    ) {
        return assertThrows(ConfigDeserializationException.class, () ->
            deserializer().deserialize(source, cls)
        );
    }

    protected final void assertSingleError(
        ConfigDeserializationException ex,
        ConfigErrorKind errorType,
        String... expectedPathSegments
    ) {
        assertEquals(1, ex.getErrors().size(), "Expected exactly 1 error");
        assertEquals(
            errorType,
            ex.getErrors().get(0).getErrorKind(),
            "Unexpected error type"
        );
        assertPathSegments(ex, 0, expectedPathSegments);
    }

    protected final void assertErrorType(
        ConfigDeserializationException ex,
        int index,
        ConfigErrorKind errorType
    ) {
        assertEquals(
            errorType,
            ex.getErrors().get(index).getErrorKind(),
            "Unexpected error type at index " + index
        );
    }

    protected final void assertPathSegments(
        ConfigDeserializationException ex,
        int index,
        String... expectedPathSegments
    ) {
        List<String> actual = ex.getErrors().get(index).getPathSegments();
        assertEquals(Arrays.asList(expectedPathSegments), actual);
    }

    protected final void assertErrorTreeRootChildren(
        ConfigDeserializationException ex,
        String... expectedSegments
    ) {
        ConfigDeserializationException.PathNode root = ex.getErrorPathTree();
        assertEquals("$", root.getSegment());

        Set<String> actual = new HashSet<>();
        for (ConfigDeserializationException.PathNode child : root.getChildren()) {
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

    static final class CfgStringLeaf { public NonEmptyString name; }
    static final class CfgIntLeaf { public PositiveInteger n; }
    static final class CfgDoubleLeaf { public PositiveDouble x; }
    static final class CfgEnum { public Mode mode; }
    static final class CfgBooleanLeaf { public Boolean enabled; }
    static final class CfgInjected { public Mode mode; public NonEmptyString name; }

    static final class CfgMissingRequired { public NonEmptyString name; }
    static final class CfgMissingOptional { public Optional<NonEmptyString> name; }
    static final class CfgOptionalHasDefaultPresent {
        public Optional<NonEmptyString> name = Optional.of(new NonEmptyString("x"));
    }
    static final class CfgDefaultValue { public NonEmptyString name = new NonEmptyString("default"); }
    static final class CfgExtraKeysIgnored { public NonEmptyString name; }
    static final class CfgDefaultNestedObjectKept {
        public NestedDefaultsOrOptional db = new NestedDefaultsOrOptional();
    }

    static final class CfgNestedPort { public NestedPort db; }
    static final class CfgNestedDefaultsOrOptional { public NestedDefaultsOrOptional db; }
    static final class CfgEmptyTableButNestedHasRequiredField { public NestedPort db; }

    public static final class NoNoArgNested {
        public NonEmptyString x;
        public NoNoArgNested(NonEmptyString x) { this.x = x; }
    }
    static final class CfgBadNestedNoNoArg { public NoNoArgNested bad; }
    static final class CfgNestedProvidedAsString { public NestedPort db; }

    static final class CfgOptionalOfComplex { public Optional<NestedHost> db; }
    static final class CfgOptionalLeafBadValue { public Optional<PositiveInteger> n; }

    static final class CfgListOfLeaf { public List<NonEmptyString> tags; }
    static final class CfgSetOfLeaf { public Set<NonEmptyString> tags; }
    static final class CfgMapOfLeaf { public Map<NonEmptyString, PositiveInteger> limits; }
    static final class CfgMapKeyWrongType { public Map<PositiveInteger, PositiveInteger> limits; }
    static final class CfgListOfComplex { public List<ItemName> items; }
    static final class CfgMapOfComplex { public Map<NonEmptyString, ItemN> items; }
    static final class CfgMissingRequiredList { public List<NonEmptyString> tags; }
    static final class CfgMissingRequiredMap { public Map<NonEmptyString, PositiveInteger> limits; }
    static final class CfgDefaultListKept {
        public List<NonEmptyString> tags = new ArrayList<>(Arrays.asList(new NonEmptyString("d")));
    }
    static final class CfgDefaultMapKept {
        public Map<NonEmptyString, PositiveInteger> limits = new LinkedHashMap<>();
        public CfgDefaultMapKept() { limits.put(new NonEmptyString("x"), new PositiveInteger(1)); }
    }
    static final class CfgListElementWrongType { public List<NonEmptyString> tags; }
    static final class CfgMapHasBadEntry { public Map<NonEmptyString, PositiveInteger> limits; }
    static final class CfgNestedGenericsBadInMap {
        public Map<NonEmptyString, List<NonEmptyString>> bad;
    }

    static final class CfgCollectAllErrors {
        public NonEmptyString a;
        public PositiveInteger b;
    }

    static class BaseCfg { public NonEmptyString base; }
    static final class DerivedCfg extends BaseCfg { public PositiveInteger child; }
    static final class CfgPrimitiveFieldNotSupported { public int n; }
    static final class CfgRootIsComplex { public NonEmptyString name; }

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

    static final class CfgOptionalLeafNoDefault { public Optional<PositiveInteger> n; }
    static final class CfgOptionalLeafWithDefaultPresent {
        public Optional<NonEmptyString> name = Optional.of(new NonEmptyString("default"));
    }
    static final class CfgOptionalLeafWithDefaultEmpty {
        public Optional<NonEmptyString> name = Optional.empty();
    }
    static final class CfgOptionalComplexWithDefaultPresent {
        public Optional<TestMe.OInnerTest> onnie = Optional.of(new TestMe.OInnerTest());
    }
    static final class CfgOptionalComplexNoDefault { public Optional<TestMe.OInnerTest> onnie; }
    static final class CfgOptionalComplexInnerFieldMutation {
        public Optional<TestMe.OInnerTest> onnie = Optional.of(new TestMe.OInnerTest());
    }
    public static final class NoNoArgNested2 {
        public NonEmptyString x;
        public NoNoArgNested2(NonEmptyString x) { this.x = x; }
    }
    static final class CfgOptionalBadInnerNoNoArg { public Optional<NoNoArgNested2> bad; }
}
