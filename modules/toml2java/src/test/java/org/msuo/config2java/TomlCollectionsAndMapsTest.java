package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlCollectionsAndMapsTest extends TomlContractSupport {

    @Test
    void listOfLeafTypes_supported() {
        ListOfLeaf cfg = ok("tags = ['a', 'b']", ListOfLeaf.class);
        assertEquals(2, cfg.tags.size());
        assertEquals("a", cfg.tags.get(0).value);
    }

    @Test
    void setOfLeafTypes_supported_andDeduplicates() {
        SetOfLeaf cfg = ok("tags = ['a', 'b', 'a']", SetOfLeaf.class);
        assertEquals(2, cfg.tags.size());
    }

    @Test
    void mapOfLeafTypes_supported() {
        MapOfLeaf cfg = ok("[limits]\nfoo = 1\nbar = 2", MapOfLeaf.class);
        assertEquals(2, cfg.limits.size());
    }

    @Test
    void mapKeyWrongType_reportsError_atKeyPathWithBraces() {
        ConfigDeserializationException ex = fails("[limits]\nfoo = 1", MapKeyWrongType.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "limits", "{foo}");
    }

    @Test
    void listCanContainComplexTypes() {
        ListOfComplex cfg = ok("[[items]]\nname = 'a'\n[[items]]\nname = 'b'", ListOfComplex.class);
        assertEquals(2, cfg.items.size());
        assertEquals("b", cfg.items.get(1).name.value);
    }

    @Test
    void mapCanContainComplexTypes() {
        MapOfComplex cfg = ok("[items.x]\nn = 1\n[items.y]\nn = 2", MapOfComplex.class);
        assertEquals(2, cfg.items.size());
    }

    @Test
    void missingRequiredList_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("", MissingRequiredList.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "tags");
    }

    @Test
    void missingRequiredMap_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("", MissingRequiredMap.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "limits");
    }

    @Test
    void defaultList_isKeptWhenKeyMissing() {
        DefaultListKept cfg = ok("", DefaultListKept.class);
        assertEquals(1, cfg.tags.size());
        assertEquals("d", cfg.tags.get(0).value);
    }

    @Test
    void defaultMap_isKeptWhenKeyMissing() {
        DefaultMapKept cfg = ok("", DefaultMapKept.class);
        assertEquals(1, cfg.limits.size());
    }

    @Test
    void listElementWrongType_reportsError_atElementPath() {
        ConfigDeserializationException ex = fails("tags = [1]", ListElementWrongType.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "tags", "[1]");
    }

    @Test
    void mapBadEntry_reportsError() {
        ConfigDeserializationException ex = fails("[limits]\nok = 1\nbad = 0", MapHasBadEntry.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "limits", "[bad]");
    }

    @Test
    void nestedGenericsInMap_areSupported() {
        NestedGenericsMapValue cfg = ok("[bad]\nfoo = ['a', 'b']", NestedGenericsMapValue.class);
        assertEquals(1, cfg.bad.size());
        assertEquals(2, cfg.bad.get(new NonEmptyString("foo")).size());
    }

    @Test
    void nestedGenericClassField_isSupported() {
        NestedGenericObjectGraph cfg = ok(
            "[[foo.value]]\npayload = 'a'\n[[foo.value]]\npayload = 'b'",
            NestedGenericObjectGraph.class
        );
        assertEquals("a", cfg.foo.value.get(0).payload);
        assertEquals("b", cfg.foo.value.get(1).payload);
    }

    @Test
    void nestedGenericMapKeyAndValue_areSupported() {
        NestedGenericKeyedListMap cfg = ok(
            "[values]\nk1 = ['a', 'b']\nk2 = ['x', 'y']",
            NestedGenericKeyedListMap.class
        );
        assertEquals(2, cfg.values.size());
        assertEquals(2, cfg.values.get(new StringConstructedGenericKey<Integer>("k1")).size());
        assertEquals("x", cfg.values.get(new StringConstructedGenericKey<Integer>("k2")).get(0));
    }

    @Test
    void nestedGenericClass_deepInvalidLeaf_reportsError() {
        ConfigDeserializationException ex = fails(
            "[[foo.value]]\npayload = 1",
            NestedGenericObjectGraph.class
        );
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "foo", "value", "[1]", "payload");
    }

    @Test
    void nestedGenericMap_deepInvalidLeaf_reportsError() {
        ConfigDeserializationException ex = fails(
            "[values]\nk1 = [1]",
            NestedGenericKeyedListMap.class
        );
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "values", "[k1]", "[1]");
    }
}
