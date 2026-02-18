package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class TomlOptionalSemanticsTest extends TomlContractSupport {

    @Test
    void missingOptional_withoutDefault_defaultsToEmpty() {
        OptionalLeafNoDefault data = ok("", OptionalLeafNoDefault.class);
        assertEquals(Optional.empty(), data.n);
    }

    @Test
    void optionalComplex_nilOrMissing_keepsDefaultPresent() {
        OptionalComplexWithDefaultPresent data = ok(
            "",
            OptionalComplexWithDefaultPresent.class
        );
        assertTrue(data.onnie.isPresent());
        assertEquals("c", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void missingOptional_keepsDefaultPresent() {
        OptionalLeafWithDefaultPresent data = ok("", OptionalLeafWithDefaultPresent.class);
        assertTrue(data.name.isPresent());
        assertEquals("default", data.name.get().value);
    }

    @Test
    void missingOptional_keepsDefaultEmpty() {
        OptionalLeafWithDefaultEmpty data = ok("", OptionalLeafWithDefaultEmpty.class);
        assertEquals(Optional.empty(), data.name);
    }

    @Test
    void providedValidOptionalLeaf_parsesPresent() {
        OptionalLeafNoDefault data = ok("n = 3", OptionalLeafNoDefault.class);
        assertTrue(data.n.isPresent());
        assertEquals(Integer.valueOf(3), data.n.get().value);
    }

    @Test
    void providedInvalidOptionalLeaf_fails_andDoesNotOverwriteDefaultPresent() {
        DataDeserializationException ex = fails("name = ''", OptionalLeafWithDefaultPresent.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void providedInvalidOptionalLeaf_fails_atOptionalPath() {
        DataDeserializationException ex = fails("n = 0", OptionalLeafNoDefault.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void optionalComplex_missing_withoutDefault_defaultsToEmpty() {
        OptionalComplexNoDefault data = ok("", OptionalComplexNoDefault.class);
        assertEquals(Optional.empty(), data.onnie);
    }

    @Test
    void optionalComplex_missing_keepsDefaultPresent() {
        OptionalComplexWithDefaultPresent data = ok("", OptionalComplexWithDefaultPresent.class);
        assertTrue(data.onnie.isPresent());
        assertEquals("c", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void optionalComplex_provided_overridesDefault_andMutatesInnerFields() {
        OptionalComplexInnerFieldMutation data = ok("[onnie]\nc = 'k'", OptionalComplexInnerFieldMutation.class);
        assertTrue(data.onnie.isPresent());
        assertEquals("k", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void providedNilOptional_isIndistinguishableFromMissing_andKeepsDefaultPresent() {
        OptionalLeafWithDefaultPresent data = ok("", OptionalLeafWithDefaultPresent.class);
        assertTrue(data.name.isPresent());
        assertEquals("default", data.name.get().value);
    }

    @Test
    void optionalComplex_providedEmptyTable_isPresent_andKeepsInnerDefaults() {
        OptionalComplexNoDefault data = ok("[onnie]", OptionalComplexNoDefault.class);

        assertTrue(data.onnie.isPresent());
        assertEquals("c", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void optionalComplex_providedButInnerCannotInstantiate_fails() {
        DataDeserializationException ex = fails("[bad]\nx = 'ok'", OptionalBadInnerNoNoArg.class);
        assertSingleError(ex, DataErrorTypes.NoNoArgCtor.class, "bad");
    }
}
