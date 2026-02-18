package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class JsonCollectionsAndMapsTest extends JsonContractSupport {

    @Test
    void listOfLeafTypes_supported() {
        ListOfLeaf data = ok("{\"tags\":[\"a\",\"b\"]}", ListOfLeaf.class);
        assertEquals(2, data.tags.size());
        assertEquals("a", data.tags.get(0).value);
    }

    @Test
    void setOfLeafTypes_supported_andDeduplicates() {
        SetOfLeaf data = ok("{\"tags\":[\"a\",\"b\",\"a\"]}", SetOfLeaf.class);
        assertEquals(2, data.tags.size());
    }

    @Test
    void mapOfLeafTypes_supported() {
        MapOfLeaf data = ok("{\"limits\":{\"foo\":1,\"bar\":2}}", MapOfLeaf.class);
        assertEquals(2, data.limits.size());
    }

    @Test
    void mapKeyWrongType_reportsError_atKeyPathWithBraces() {
        DataDeserializationException ex = fails("{\"limits\":{\"foo\":1}}", MapKeyWrongType.class);
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "limits", "{foo}");
    }

    @Test
    void listCanContainComplexTypes() {
        ListOfComplex data = ok("{\"items\":[{\"name\":\"a\"},{\"name\":\"b\"}]}", ListOfComplex.class);
        assertEquals(2, data.items.size());
        assertEquals("b", data.items.get(1).name.value);
    }

    @Test
    void mapCanContainComplexTypes() {
        MapOfComplex data = ok("{\"items\":{\"x\":{\"n\":1},\"y\":{\"n\":2}}}", MapOfComplex.class);
        assertEquals(2, data.items.size());
    }

    @Test
    void missingRequiredList_withoutDefault_fails() {
        DataDeserializationException ex = fails("{}", MissingRequiredList.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "tags");
    }

    @Test
    void missingRequiredMap_withoutDefault_fails() {
        DataDeserializationException ex = fails("{}", MissingRequiredMap.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "limits");
    }

    @Test
    void defaultList_isKeptWhenKeyMissing() {
        DefaultListKept data = ok("{}", DefaultListKept.class);
        assertEquals(1, data.tags.size());
        assertEquals("d", data.tags.get(0).value);
    }

    @Test
    void defaultMap_isKeptWhenKeyMissing() {
        DefaultMapKept data = ok("{}", DefaultMapKept.class);
        assertEquals(1, data.limits.size());
    }

    @Test
    void listElementWrongType_reportsError_atElementPath() {
        DataDeserializationException ex = fails("{\"tags\":[1]}", ListElementWrongType.class);
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "tags", "[1]");
    }

    @Test
    void mapBadEntry_reportsError() {
        DataDeserializationException ex = fails("{\"limits\":{\"ok\":1,\"bad\":0}}", MapHasBadEntry.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "limits", "[bad]");
    }

    @Test
    void nestedGenericsInMap_areSupported() {
        NestedGenericsMapValue data = ok("{\"bad\":{\"foo\":[\"a\",\"b\"]}}", NestedGenericsMapValue.class);
        assertEquals(1, data.bad.size());
        assertEquals(2, data.bad.get(new NonEmptyString("foo")).size());
    }

    @Test
    void nestedGenericClassField_isSupported() {
        NestedGenericObjectGraph data = ok(
            "{\"foo\":{\"value\":[{\"payload\":\"a\"},{\"payload\":\"b\"}]}}",
            NestedGenericObjectGraph.class
        );
        assertEquals("a", data.foo.value.get(0).payload);
        assertEquals("b", data.foo.value.get(1).payload);
    }

    @Test
    void nestedGenericMapKeyAndValue_areSupported() {
        NestedGenericKeyedListMap data = ok(
            "{\"values\":{\"k1\":[\"a\",\"b\"],\"k2\":[\"x\",\"y\"]}}",
            NestedGenericKeyedListMap.class
        );
        assertEquals(2, data.values.size());
        assertEquals(2, data.values.get(new StringConstructedGenericKey<Integer>("k1")).size());
        assertEquals("x", data.values.get(new StringConstructedGenericKey<Integer>("k2")).get(0));
    }

    @Test
    void nestedGenericClass_deepInvalidLeaf_reportsError() {
        DataDeserializationException ex = fails(
            "{\"foo\":{\"value\":[{\"payload\":1}]}}",
            NestedGenericObjectGraph.class
        );
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "foo", "value", "[1]", "payload");
    }

    @Test
    void nestedGenericMap_deepInvalidLeaf_reportsError() {
        DataDeserializationException ex = fails(
            "{\"values\":{\"k1\":[1]}}",
            NestedGenericKeyedListMap.class
        );
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "values", "[k1]", "[1]");
    }
}
