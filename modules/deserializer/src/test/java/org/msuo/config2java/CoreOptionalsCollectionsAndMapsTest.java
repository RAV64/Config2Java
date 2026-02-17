package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CoreOptionalsCollectionsAndMapsTest
    extends CoreMapperContractSupport {

    @Test
    void optionalLeaf_missing_becomesEmpty() {
        OptionalLeaf cfg = ok(obj(), OptionalLeaf.class);
        assertEquals(Optional.empty(), cfg.n);
    }

    @Test
    void optionalLeaf_null_becomesEmpty() {
        OptionalLeaf cfg = ok(obj("n", null), OptionalLeaf.class);
        assertEquals(Optional.empty(), cfg.n);
    }

    @Test
    void optionalLeaf_badValue_reportsCtorRejected() {
        ConfigDeserializationException ex = fails(obj("n", 0), OptionalLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void optionalDefaultPresent_andMissing_keepsDefault() {
        OptionalLeafWithDefault cfg = ok(obj(), OptionalLeafWithDefault.class);
        assertEquals(Optional.of(new NonEmptyString("x")), cfg.name);
    }

    @Test
    void optionalComplex_success() {
        OptionalComplex cfg = ok(obj("db", obj("port", 15432)), OptionalComplex.class);
        assertTrue(cfg.db.isPresent());
        assertEquals(Integer.valueOf(15432), cfg.db.get().port.value);
    }

    @Test
    void listSetMap_success() {
        ListOfLeaf listCfg = ok(obj("tags", arr("a", "b")), ListOfLeaf.class);
        SetOfLeaf setCfg = ok(obj("tags", arr("a", "b", "a")), SetOfLeaf.class);
        MapOfLeaf mapCfg = ok(obj("limits", obj("api", 10, "db", 20)), MapOfLeaf.class);

        assertEquals(2, listCfg.tags.size());
        assertEquals(2, setCfg.tags.size());
        assertEquals(2, mapCfg.limits.size());
    }

    @Test
    void listElementWrongType_reportsNoOneArgCtor() {
        ConfigDeserializationException ex = fails(obj("tags", arr(1)), ListOfLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "tags", "[1]");
    }

    @Test
    void mapKeyWrongType_reportsNoOneArgCtorOnKeyPath() {
        ConfigDeserializationException ex = fails(
            obj("limits", obj("foo", 1)),
            MapKeyWrongType.class
        );
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "limits", "{foo}");
    }
}
