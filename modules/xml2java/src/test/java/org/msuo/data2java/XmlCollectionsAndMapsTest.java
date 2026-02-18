package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlCollectionsAndMapsTest extends XmlContractSupport {

    @Test
    void listOfLeafTypes_supported() {
        ListOfLeaf data = ok("<data><tags>a</tags><tags>b</tags></data>", ListOfLeaf.class);
        assertEquals(2, data.tags.size());
        assertEquals("a", data.tags.get(0).value);
    }

    @Test
    void setOfLeafTypes_supported_andDeduplicates() {
        SetOfLeaf data = ok("<data><tags>a</tags><tags>b</tags><tags>a</tags></data>", SetOfLeaf.class);
        assertEquals(2, data.tags.size());
    }

    @Test
    void mapOfLeafTypes_supported() {
        MapOfLeaf data = ok("<data><limits><foo>1</foo><bar>2</bar></limits></data>", MapOfLeaf.class);
        assertEquals(2, data.limits.size());
    }

    @Test
    void mapKeyWrongType_reportsError_atKeyPathWithBraces() {
        DataDeserializationException ex = fails("<data><limits><foo>1</foo></limits></data>", MapKeyWrongType.class);
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "limits", "{foo}");
    }

    @Test
    void listCanContainComplexTypes() {
        ListOfComplex data = ok("<data><items><name>a</name></items><items><name>b</name></items></data>", ListOfComplex.class);
        assertEquals(2, data.items.size());
        assertEquals("b", data.items.get(1).name.value);
    }

    @Test
    void mapCanContainComplexTypes() {
        MapOfComplex data = ok("<data><items><x><n>1</n></x><y><n>2</n></y></items></data>", MapOfComplex.class);
        assertEquals(2, data.items.size());
    }

    @Test
    void missingRequiredList_withoutDefault_fails() {
        DataDeserializationException ex = fails("<data/>", MissingRequiredList.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "tags");
    }

    @Test
    void missingRequiredMap_withoutDefault_fails() {
        DataDeserializationException ex = fails("<data/>", MissingRequiredMap.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "limits");
    }

    @Test
    void defaultList_isKeptWhenKeyMissing() {
        DefaultListKept data = ok("<data/>", DefaultListKept.class);
        assertEquals(1, data.tags.size());
        assertEquals("d", data.tags.get(0).value);
    }

    @Test
    void defaultMap_isKeptWhenKeyMissing() {
        DefaultMapKept data = ok("<data/>", DefaultMapKept.class);
        assertEquals(1, data.limits.size());
    }

    @Test
    void listElementWrongType_reportsError_atElementPath() {
        DataDeserializationException ex = fails("<data><tags>1</tags><tags>a</tags></data>", ListElementWrongType.class);
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "tags", "[1]");
    }

    @Test
    void mapBadEntry_reportsError() {
        DataDeserializationException ex = fails("<data><limits><ok>1</ok><bad>0</bad></limits></data>", MapHasBadEntry.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "limits", "[bad]");
    }

    @Test
    void nestedGenericsInMap_areSupported() {
        NestedGenericsMapValue data = ok(
            "<data><bad><foo>a</foo><foo>b</foo></bad></data>",
            NestedGenericsMapValue.class
        );
        assertEquals(1, data.bad.size());
        assertEquals(2, data.bad.get(new NonEmptyString("foo")).size());
    }

    @Test
    void nestedGenericClassField_isSupported() {
        NestedGenericObjectGraph data = ok(
            "<data><foo><value><payload>a</payload></value><value><payload>b</payload></value></foo></data>",
            NestedGenericObjectGraph.class
        );
        assertEquals("a", data.foo.value.get(0).payload);
        assertEquals("b", data.foo.value.get(1).payload);
    }

    @Test
    void nestedGenericMapKeyAndValue_areSupported() {
        NestedGenericKeyedListMap data = ok(
            "<data><values><k1>a</k1><k1>b</k1><k2>x</k2><k2>y</k2></values></data>",
            NestedGenericKeyedListMap.class
        );
        assertEquals(2, data.values.size());
        assertEquals(2, data.values.get(new StringConstructedGenericKey<Integer>("k1")).size());
        assertEquals("x", data.values.get(new StringConstructedGenericKey<Integer>("k2")).get(0));
    }

    @Test
    void nestedGenericClass_deepInvalidLeaf_reportsError() {
        DataDeserializationException ex = fails(
            "<data><foo><value>bad</value></foo></data>",
            NestedGenericObjectGraph.class
        );
        assertSingleError(ex, DataErrorTypes.CollectionExpected.class, "foo", "value");
    }

    @Test
    void nestedGenericMap_deepInvalidLeaf_reportsError() {
        DataDeserializationException ex = fails(
            "<data><values><k1>1</k1></values></data>",
            NestedGenericKeyedListMap.class
        );
        assertSingleError(ex, DataErrorTypes.CollectionExpected.class, "values", "[k1]");
    }
}
