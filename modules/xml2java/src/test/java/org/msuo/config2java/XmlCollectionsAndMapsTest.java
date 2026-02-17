package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlCollectionsAndMapsTest extends XmlContractSupport {

    @Test
    void listOfLeafTypes_supported() {
        ListOfLeaf cfg = ok("<config><tags>a</tags><tags>b</tags></config>", ListOfLeaf.class);
        assertEquals(2, cfg.tags.size());
        assertEquals("a", cfg.tags.get(0).value);
    }

    @Test
    void setOfLeafTypes_supported_andDeduplicates() {
        SetOfLeaf cfg = ok("<config><tags>a</tags><tags>b</tags><tags>a</tags></config>", SetOfLeaf.class);
        assertEquals(2, cfg.tags.size());
    }

    @Test
    void mapOfLeafTypes_supported() {
        MapOfLeaf cfg = ok("<config><limits><foo>1</foo><bar>2</bar></limits></config>", MapOfLeaf.class);
        assertEquals(2, cfg.limits.size());
    }

    @Test
    void mapKeyWrongType_reportsError_atKeyPathWithBraces() {
        ConfigDeserializationException ex = fails("<config><limits><foo>1</foo></limits></config>", MapKeyWrongType.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "limits", "{foo}");
    }

    @Test
    void listCanContainComplexTypes() {
        ListOfComplex cfg = ok("<config><items><name>a</name></items><items><name>b</name></items></config>", ListOfComplex.class);
        assertEquals(2, cfg.items.size());
        assertEquals("b", cfg.items.get(1).name.value);
    }

    @Test
    void mapCanContainComplexTypes() {
        MapOfComplex cfg = ok("<config><items><x><n>1</n></x><y><n>2</n></y></items></config>", MapOfComplex.class);
        assertEquals(2, cfg.items.size());
    }

    @Test
    void missingRequiredList_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("<config/>", MissingRequiredList.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "tags");
    }

    @Test
    void missingRequiredMap_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("<config/>", MissingRequiredMap.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "limits");
    }

    @Test
    void defaultList_isKeptWhenKeyMissing() {
        DefaultListKept cfg = ok("<config/>", DefaultListKept.class);
        assertEquals(1, cfg.tags.size());
        assertEquals("d", cfg.tags.get(0).value);
    }

    @Test
    void defaultMap_isKeptWhenKeyMissing() {
        DefaultMapKept cfg = ok("<config/>", DefaultMapKept.class);
        assertEquals(1, cfg.limits.size());
    }

    @Test
    void listElementWrongType_reportsError_atElementPath() {
        ConfigDeserializationException ex = fails("<config><tags>1</tags><tags>a</tags></config>", ListElementWrongType.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "tags", "[1]");
    }

    @Test
    void mapBadEntry_reportsError() {
        ConfigDeserializationException ex = fails("<config><limits><ok>1</ok><bad>0</bad></limits></config>", MapHasBadEntry.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "limits", "[bad]");
    }

    @Test
    void nestedGenericsInMap_areSupported() {
        NestedGenericsMapValue cfg = ok(
            "<config><bad><foo>a</foo><foo>b</foo></bad></config>",
            NestedGenericsMapValue.class
        );
        assertEquals(1, cfg.bad.size());
        assertEquals(2, cfg.bad.get(new NonEmptyString("foo")).size());
    }

    @Test
    void nestedGenericClassField_isSupported() {
        NestedGenericObjectGraph cfg = ok(
            "<config><foo><value><payload>a</payload></value><value><payload>b</payload></value></foo></config>",
            NestedGenericObjectGraph.class
        );
        assertEquals("a", cfg.foo.value.get(0).payload);
        assertEquals("b", cfg.foo.value.get(1).payload);
    }

    @Test
    void nestedGenericMapKeyAndValue_areSupported() {
        NestedGenericKeyedListMap cfg = ok(
            "<config><values><k1>a</k1><k1>b</k1><k2>x</k2><k2>y</k2></values></config>",
            NestedGenericKeyedListMap.class
        );
        assertEquals(2, cfg.values.size());
        assertEquals(2, cfg.values.get(new StringConstructedGenericKey<Integer>("k1")).size());
        assertEquals("x", cfg.values.get(new StringConstructedGenericKey<Integer>("k2")).get(0));
    }

    @Test
    void nestedGenericClass_deepInvalidLeaf_reportsError() {
        ConfigDeserializationException ex = fails(
            "<config><foo><value>bad</value></foo></config>",
            NestedGenericObjectGraph.class
        );
        assertSingleError(ex, ConfigErrorTypes.CollectionExpected.class, "foo", "value");
    }

    @Test
    void nestedGenericMap_deepInvalidLeaf_reportsError() {
        ConfigDeserializationException ex = fails(
            "<config><values><k1>1</k1></values></config>",
            NestedGenericKeyedListMap.class
        );
        assertSingleError(ex, ConfigErrorTypes.CollectionExpected.class, "values", "[k1]");
    }
}
