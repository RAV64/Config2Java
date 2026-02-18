package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CoreOptionalsCollectionsAndMapsTest
    extends CoreMapperContractSupport {

    @Test
    void optionalLeaf_missing_becomesEmpty() {
        OptionalLeaf data = ok(obj(), OptionalLeaf.class);
        assertEquals(Optional.empty(), data.n);
    }

    @Test
    void optionalLeaf_null_becomesEmpty() {
        OptionalLeaf data = ok(obj("n", null), OptionalLeaf.class);
        assertEquals(Optional.empty(), data.n);
    }

    @Test
    void optionalLeaf_badValue_reportsCtorRejected() {
        DataDeserializationException ex = fails(obj("n", 0), OptionalLeaf.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void optionalDefaultPresent_andMissing_keepsDefault() {
        OptionalLeafWithDefault data = ok(obj(), OptionalLeafWithDefault.class);
        assertEquals(Optional.of(new NonEmptyString("x")), data.name);
    }

    @Test
    void optionalComplex_success() {
        OptionalComplex data = ok(obj("db", obj("port", 15432)), OptionalComplex.class);
        assertTrue(data.db.isPresent());
        assertEquals(Integer.valueOf(15432), data.db.get().port.value);
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
        DataDeserializationException ex = fails(obj("tags", arr(1)), ListOfLeaf.class);
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "tags", "[1]");
    }

    @Test
    void mapKeyWrongType_reportsNoOneArgCtorOnKeyPath() {
        DataDeserializationException ex = fails(
            obj("limits", obj("foo", 1)),
            MapKeyWrongType.class
        );
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "limits", "{foo}");
    }

    @Test
    void listGivenScalar_reportsCollectionExpected() {
        DataDeserializationException ex = fails(
            obj("tags", 1),
            ListOfLeaf.class
        );
        assertSingleError(ex, DataErrorTypes.CollectionExpected.class, "tags");
    }

    @Test
    void mapGivenScalar_reportsMapExpected() {
        DataDeserializationException ex = fails(
            obj("limits", 1),
            MapOfLeaf.class
        );
        assertSingleError(ex, DataErrorTypes.MapExpected.class, "limits");
    }
}
