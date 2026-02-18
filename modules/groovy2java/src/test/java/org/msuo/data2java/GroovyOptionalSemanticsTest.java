package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class GroovyOptionalSemanticsTest extends GroovyContractSupport {

    @Test
    void missingOptional_withoutDefault_defaultsToEmpty() {
        OptionalLeafNoDefault data = ok("return [:]", OptionalLeafNoDefault.class);
        assertEquals(Optional.empty(), data.n);
    }

    @Test
    void optionalComplex_nilOrMissing_keepsDefaultPresent() {
        OptionalComplexWithDefaultPresent data = ok(
            "return [onnie: null]",
            OptionalComplexWithDefaultPresent.class
        );
        assertEquals(Optional.empty(), data.onnie);
    }

    @Test
    void missingOptional_keepsDefaultPresent() {
        OptionalLeafWithDefaultPresent data = ok("return [:]", OptionalLeafWithDefaultPresent.class);
        assertTrue(data.name.isPresent());
        assertEquals("default", data.name.get().value);
    }

    @Test
    void missingOptional_keepsDefaultEmpty() {
        OptionalLeafWithDefaultEmpty data = ok("return [:]", OptionalLeafWithDefaultEmpty.class);
        assertEquals(Optional.empty(), data.name);
    }

    @Test
    void providedValidOptionalLeaf_parsesPresent() {
        OptionalLeafNoDefault data = ok("return [n: 3]", OptionalLeafNoDefault.class);
        assertTrue(data.n.isPresent());
        assertEquals(Integer.valueOf(3), data.n.get().value);
    }

    @Test
    void providedInvalidOptionalLeaf_fails_andDoesNotOverwriteDefaultPresent() {
        DataDeserializationException ex = fails("return [name: '']", OptionalLeafWithDefaultPresent.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void providedInvalidOptionalLeaf_fails_atOptionalPath() {
        DataDeserializationException ex = fails("return [n: 0]", OptionalLeafNoDefault.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void optionalComplex_missing_withoutDefault_defaultsToEmpty() {
        OptionalComplexNoDefault data = ok("return [:]", OptionalComplexNoDefault.class);
        assertEquals(Optional.empty(), data.onnie);
    }

    @Test
    void optionalComplex_missing_keepsDefaultPresent() {
        OptionalComplexWithDefaultPresent data = ok("return [:]", OptionalComplexWithDefaultPresent.class);
        assertTrue(data.onnie.isPresent());
        assertEquals("c", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void optionalComplex_provided_overridesDefault_andMutatesInnerFields() {
        OptionalComplexInnerFieldMutation data = ok("return [onnie: [c: 'k']]", OptionalComplexInnerFieldMutation.class);
        assertTrue(data.onnie.isPresent());
        assertEquals("k", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void providedNilOptional_isIndistinguishableFromMissing_andKeepsDefaultPresent() {
        OptionalLeafWithDefaultPresent data = ok("return [name: null]", OptionalLeafWithDefaultPresent.class);
        assertEquals(Optional.empty(), data.name);
    }

    @Test
    void optionalComplex_providedEmptyTable_isPresent_andKeepsInnerDefaults() {
        OptionalComplexNoDefault data = ok("return [onnie: [:]]", OptionalComplexNoDefault.class);

        assertTrue(data.onnie.isPresent());
        assertEquals("c", data.onnie.get().c);
        assertEquals(Integer.valueOf(5), data.onnie.get().d);
    }

    @Test
    void optionalComplex_providedButInnerCannotInstantiate_fails() {
        DataDeserializationException ex = fails("return [bad: [x: 'ok']]", OptionalBadInnerNoNoArg.class);
        assertSingleError(ex, DataErrorTypes.NoNoArgCtor.class, "bad");
    }

    @Test
    void optionalComplex_withUnknownNestedField_reportsUnknownField() {
        DataDeserializationException ex = fails(
            "return [onnie: [c: 'k', extra: 1]]",
            OptionalComplexNoDefault.class
        );
        assertSingleError(ex, DataErrorTypes.UnknownField.class, "onnie", "extra");
    }
}
