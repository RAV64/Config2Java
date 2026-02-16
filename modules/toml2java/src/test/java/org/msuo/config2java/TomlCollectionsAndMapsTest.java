package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlCollectionsAndMapsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new TomlDeserializer();
    }


    @Test
    void listOfLeafTypes_supported() {
        CfgListOfLeaf cfg = ok("tags = ['a', 'b']", CfgListOfLeaf.class);
        assertEquals(2, cfg.tags.size());
        assertEquals("a", cfg.tags.get(0).value);
    }

    @Test
    void setOfLeafTypes_supported_andDeduplicates() {
        CfgSetOfLeaf cfg = ok("tags = ['a', 'b', 'a']", CfgSetOfLeaf.class);
        assertEquals(2, cfg.tags.size());
    }

    @Test
    void mapOfLeafTypes_supported() {
        CfgMapOfLeaf cfg = ok("[limits]\nfoo = 1\nbar = 2", CfgMapOfLeaf.class);
        assertEquals(2, cfg.limits.size());
    }

    @Test
    void mapKeyWrongType_reportsError_atKeyPathWithBraces() {
        ConfigDeserializationException ex = fails("[limits]\nfoo = 1", CfgMapKeyWrongType.class);
        assertSingleError(ex, ConfigErrorKind.NoOneArgCtor, "limits", "{foo}");
    }

    @Test
    void listCanContainComplexTypes() {
        CfgListOfComplex cfg = ok("[[items]]\nname = 'a'\n[[items]]\nname = 'b'", CfgListOfComplex.class);
        assertEquals(2, cfg.items.size());
        assertEquals("b", cfg.items.get(1).name.value);
    }

    @Test
    void mapCanContainComplexTypes() {
        CfgMapOfComplex cfg = ok("[items.x]\nn = 1\n[items.y]\nn = 2", CfgMapOfComplex.class);
        assertEquals(2, cfg.items.size());
    }

    @Test
    void missingRequiredList_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("", CfgMissingRequiredList.class);
        assertSingleError(ex, ConfigErrorKind.MissingRequiredField, "tags");
    }

    @Test
    void missingRequiredMap_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("", CfgMissingRequiredMap.class);
        assertSingleError(ex, ConfigErrorKind.MissingRequiredField, "limits");
    }

    @Test
    void defaultList_isKeptWhenKeyMissing() {
        CfgDefaultListKept cfg = ok("", CfgDefaultListKept.class);
        assertEquals(1, cfg.tags.size());
        assertEquals("d", cfg.tags.get(0).value);
    }

    @Test
    void defaultMap_isKeptWhenKeyMissing() {
        CfgDefaultMapKept cfg = ok("", CfgDefaultMapKept.class);
        assertEquals(1, cfg.limits.size());
    }

    @Test
    void listElementWrongType_reportsError_atElementPath() {
        ConfigDeserializationException ex = fails("tags = [1]", CfgListElementWrongType.class);
        assertSingleError(ex, ConfigErrorKind.NoOneArgCtor, "tags", "[1]");
    }

    @Test
    void mapBadEntry_reportsError() {
        ConfigDeserializationException ex = fails("[limits]\nok = 1\nbad = 0", CfgMapHasBadEntry.class);
        assertSingleError(ex, ConfigErrorKind.CtorRejected, "limits", "[bad]");
    }

    @Test
    void nestedGenericsInMap_areRejected() {
        ConfigDeserializationException ex = fails("[bad]\nfoo = ['a']", CfgNestedGenericsBadInMap.class);
        assertSingleError(ex, ConfigErrorKind.MapValueMustBeConcrete, "bad");
    }
}
