package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class XmlOptionalSemanticsTest extends XmlContractSupport {

    @Test
    void missingOptional_withoutDefault_defaultsToEmpty() {
        OptionalLeafNoDefault data = ok("<data/>", OptionalLeafNoDefault.class);
        assertEquals(Optional.empty(), data.n);
    }

    @Test
    void optionalComplex_nilOrMissing_keepsDefaultPresent() {
        OptionalComplexWithDefaultPresent data = ok(
            "<data/>",
            OptionalComplexWithDefaultPresent.class
        );
        assertTrue(data.onnie.isPresent());
        assertEquals("c", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void missingOptional_keepsDefaultPresent() {
        OptionalLeafWithDefaultPresent data = ok("<data/>", OptionalLeafWithDefaultPresent.class);
        assertTrue(data.name.isPresent());
        assertEquals("default", data.name.get().value);
    }

    @Test
    void missingOptional_keepsDefaultEmpty() {
        OptionalLeafWithDefaultEmpty data = ok("<data/>", OptionalLeafWithDefaultEmpty.class);
        assertEquals(Optional.empty(), data.name);
    }

    @Test
    void providedValidOptionalLeaf_parsesPresent() {
        OptionalLeafNoDefault data = ok("<data><n>3</n></data>", OptionalLeafNoDefault.class);
        assertTrue(data.n.isPresent());
        assertEquals(Integer.valueOf(3), data.n.get().value);
    }

    @Test
    void providedInvalidOptionalLeaf_fails_andDoesNotOverwriteDefaultPresent() {
        DataDeserializationException ex = fails("<data><name></name></data>", OptionalLeafWithDefaultPresent.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void providedInvalidOptionalLeaf_fails_atOptionalPath() {
        DataDeserializationException ex = fails("<data><n>0</n></data>", OptionalLeafNoDefault.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void optionalComplex_missing_withoutDefault_defaultsToEmpty() {
        OptionalComplexNoDefault data = ok("<data/>", OptionalComplexNoDefault.class);
        assertEquals(Optional.empty(), data.onnie);
    }

    @Test
    void optionalComplex_missing_keepsDefaultPresent() {
        OptionalComplexWithDefaultPresent data = ok("<data/>", OptionalComplexWithDefaultPresent.class);
        assertTrue(data.onnie.isPresent());
        assertEquals("c", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void optionalComplex_provided_overridesDefault_andMutatesInnerFields() {
        OptionalComplexInnerFieldMutation data = ok("<data><onnie><c>k</c></onnie></data>", OptionalComplexInnerFieldMutation.class);
        assertTrue(data.onnie.isPresent());
        assertEquals("k", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void providedNilOptional_isIndistinguishableFromMissing_andKeepsDefaultPresent() {
        OptionalLeafWithDefaultPresent data = ok("<data/>", OptionalLeafWithDefaultPresent.class);
        assertTrue(data.name.isPresent());
        assertEquals("default", data.name.get().value);
    }

    @Test
    void optionalComplex_providedEmptyTable_isPresent_andKeepsInnerDefaults() {
        OptionalComplexNoDefault data = ok("<data><onnie><dummy/></onnie></data>", OptionalComplexNoDefault.class);

        assertTrue(data.onnie.isPresent());
        assertEquals("c", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void optionalComplex_providedButInnerCannotInstantiate_fails() {
        DataDeserializationException ex = fails("<data><bad><x>ok</x></bad></data>", OptionalBadInnerNoNoArg.class);
        assertSingleError(ex, DataErrorTypes.NoNoArgCtor.class, "bad");
    }
}
