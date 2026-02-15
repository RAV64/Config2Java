package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class JsonCollectionsAndMapsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new JsonDeserializer();
    }


    @Test
    void listOfLeafTypes_supported() {
        CfgListOfLeaf cfg = ok("{\"tags\":[\"a\",\"b\"]}", CfgListOfLeaf.class);
        assertEquals(2, cfg.tags.size());
        assertEquals("a", cfg.tags.get(0).value);
    }

    @Test
    void setOfLeafTypes_supported_andDeduplicates() {
        CfgSetOfLeaf cfg = ok("{\"tags\":[\"a\",\"b\",\"a\"]}", CfgSetOfLeaf.class);
        assertEquals(2, cfg.tags.size());
    }

    @Test
    void mapOfLeafTypes_supported() {
        CfgMapOfLeaf cfg = ok("{\"limits\":{\"foo\":1,\"bar\":2}}", CfgMapOfLeaf.class);
        assertEquals(2, cfg.limits.size());
    }

    @Test
    void mapKeyWrongType_reportsError_atKeyPathWithBraces() {
        ConfigDeserializationException ex = fails("{\"limits\":{\"foo\":1}}", CfgMapKeyWrongType.class);
        assertSingleError(ex, "$.limits{foo}", "accepting java.lang.String");
    }

    @Test
    void listCanContainComplexTypes() {
        CfgListOfComplex cfg = ok("{\"items\":[{\"name\":\"a\"},{\"name\":\"b\"}]}", CfgListOfComplex.class);
        assertEquals(2, cfg.items.size());
        assertEquals("b", cfg.items.get(1).name.value);
    }

    @Test
    void mapCanContainComplexTypes() {
        CfgMapOfComplex cfg = ok("{\"items\":{\"x\":{\"n\":1},\"y\":{\"n\":2}}}", CfgMapOfComplex.class);
        assertEquals(2, cfg.items.size());
    }

    @Test
    void missingRequiredList_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("{}", CfgMissingRequiredList.class);
        assertSingleError(ex, "$.tags", "Missing required field");
    }

    @Test
    void missingRequiredMap_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("{}", CfgMissingRequiredMap.class);
        assertSingleError(ex, "$.limits", "Missing required field");
    }

    @Test
    void defaultList_isKeptWhenKeyMissing() {
        CfgDefaultListKept cfg = ok("{}", CfgDefaultListKept.class);
        assertEquals(1, cfg.tags.size());
        assertEquals("d", cfg.tags.get(0).value);
    }

    @Test
    void defaultMap_isKeptWhenKeyMissing() {
        CfgDefaultMapKept cfg = ok("{}", CfgDefaultMapKept.class);
        assertEquals(1, cfg.limits.size());
    }

    @Test
    void listElementWrongType_reportsError_atElementPath() {
        ConfigDeserializationException ex = fails("{\"tags\":[1]}", CfgListElementWrongType.class);
        assertSingleError(ex, "$.tags[1]", "accepting java.lang.Integer");
    }

    @Test
    void mapBadEntry_reportsError() {
        ConfigDeserializationException ex = fails("{\"limits\":{\"ok\":1,\"bad\":0}}", CfgMapHasBadEntry.class);
        assertEquals(1, ex.getErrors().size());
        assertTrue(ex.getErrors().get(0).getPath().startsWith("$.limits["));
        assertTrue(ex.getErrors().get(0).getMessage().contains("must be > 0"));
    }

    @Test
    void nestedGenericsInMap_areRejected() {
        ConfigDeserializationException ex = fails("{\"bad\":{\"foo\":[\"a\"]}}", CfgNestedGenericsBadInMap.class);
        assertSingleError(ex, "$.bad", "no nested generics");
    }
}
