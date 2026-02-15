package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlCollectionsAndMapsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new XmlDeserializer();
    }


    @Test
    void listOfLeafTypes_supported() {
        CfgListOfLeaf cfg = ok("<config><tags>a</tags><tags>b</tags></config>", CfgListOfLeaf.class);
        assertEquals(2, cfg.tags.size());
        assertEquals("a", cfg.tags.get(0).value);
    }

    @Test
    void setOfLeafTypes_supported_andDeduplicates() {
        CfgSetOfLeaf cfg = ok("<config><tags>a</tags><tags>b</tags><tags>a</tags></config>", CfgSetOfLeaf.class);
        assertEquals(2, cfg.tags.size());
    }

    @Test
    void mapOfLeafTypes_supported() {
        CfgMapOfLeaf cfg = ok("<config><limits><foo>1</foo><bar>2</bar></limits></config>", CfgMapOfLeaf.class);
        assertEquals(2, cfg.limits.size());
    }

    @Test
    void mapKeyWrongType_reportsError_atKeyPathWithBraces() {
        ConfigDeserializationException ex = fails("<config><limits><foo>1</foo></limits></config>", CfgMapKeyWrongType.class);
        assertSingleError(ex, "$.limits{foo}", "accepting java.lang.String");
    }

    @Test
    void listCanContainComplexTypes() {
        CfgListOfComplex cfg = ok("<config><items><name>a</name></items><items><name>b</name></items></config>", CfgListOfComplex.class);
        assertEquals(2, cfg.items.size());
        assertEquals("b", cfg.items.get(1).name.value);
    }

    @Test
    void mapCanContainComplexTypes() {
        CfgMapOfComplex cfg = ok("<config><items><x><n>1</n></x><y><n>2</n></y></items></config>", CfgMapOfComplex.class);
        assertEquals(2, cfg.items.size());
    }

    @Test
    void missingRequiredList_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("<config/>", CfgMissingRequiredList.class);
        assertSingleError(ex, "$.tags", "Missing required field");
    }

    @Test
    void missingRequiredMap_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("<config/>", CfgMissingRequiredMap.class);
        assertSingleError(ex, "$.limits", "Missing required field");
    }

    @Test
    void defaultList_isKeptWhenKeyMissing() {
        CfgDefaultListKept cfg = ok("<config/>", CfgDefaultListKept.class);
        assertEquals(1, cfg.tags.size());
        assertEquals("d", cfg.tags.get(0).value);
    }

    @Test
    void defaultMap_isKeptWhenKeyMissing() {
        CfgDefaultMapKept cfg = ok("<config/>", CfgDefaultMapKept.class);
        assertEquals(1, cfg.limits.size());
    }

    @Test
    void listElementWrongType_reportsError_atElementPath() {
        ConfigDeserializationException ex = fails("<config><tags>1</tags><tags>a</tags></config>", CfgListElementWrongType.class);
        assertSingleError(ex, "$.tags[1]", "accepting java.lang.Integer");
    }

    @Test
    void mapBadEntry_reportsError() {
        ConfigDeserializationException ex = fails("<config><limits><ok>1</ok><bad>0</bad></limits></config>", CfgMapHasBadEntry.class);
        assertEquals(1, ex.getErrors().size());
        assertTrue(ex.getErrors().get(0).getPath().startsWith("$.limits["));
        assertTrue(ex.getErrors().get(0).getMessage().contains("must be > 0"));
    }

    @Test
    void nestedGenericsInMap_areRejected() {
        ConfigDeserializationException ex = fails("<config><bad><foo>a</foo></bad></config>", CfgNestedGenericsBadInMap.class);
        assertSingleError(ex, "$.bad", "no nested generics");
    }
}
