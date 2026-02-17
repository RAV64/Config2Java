package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class GroovyOptionalSemanticsTest extends GroovyContractSupport {

    @Test
    void missingOptional_withoutDefault_defaultsToEmpty() {
        OptionalLeafNoDefault cfg = ok("return [:]", OptionalLeafNoDefault.class);
        assertEquals(Optional.empty(), cfg.n);
    }

    @Test
    void optionalComplex_nilOrMissing_keepsDefaultPresent() {
        OptionalComplexWithDefaultPresent cfg = ok(
            "return [onnie: null]",
            OptionalComplexWithDefaultPresent.class
        );
        assertEquals(Optional.empty(), cfg.onnie);
    }

    @Test
    void missingOptional_keepsDefaultPresent() {
        OptionalLeafWithDefaultPresent cfg = ok("return [:]", OptionalLeafWithDefaultPresent.class);
        assertTrue(cfg.name.isPresent());
        assertEquals("default", cfg.name.get().value);
    }

    @Test
    void missingOptional_keepsDefaultEmpty() {
        OptionalLeafWithDefaultEmpty cfg = ok("return [:]", OptionalLeafWithDefaultEmpty.class);
        assertEquals(Optional.empty(), cfg.name);
    }

    @Test
    void providedValidOptionalLeaf_parsesPresent() {
        OptionalLeafNoDefault cfg = ok("return [n: 3]", OptionalLeafNoDefault.class);
        assertTrue(cfg.n.isPresent());
        assertEquals(Integer.valueOf(3), cfg.n.get().value);
    }

    @Test
    void providedInvalidOptionalLeaf_fails_andDoesNotOverwriteDefaultPresent() {
        ConfigDeserializationException ex = fails("return [name: '']", OptionalLeafWithDefaultPresent.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void providedInvalidOptionalLeaf_fails_atOptionalPath() {
        ConfigDeserializationException ex = fails("return [n: 0]", OptionalLeafNoDefault.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void optionalComplex_missing_withoutDefault_defaultsToEmpty() {
        OptionalComplexNoDefault cfg = ok("return [:]", OptionalComplexNoDefault.class);
        assertEquals(Optional.empty(), cfg.onnie);
    }

    @Test
    void optionalComplex_missing_keepsDefaultPresent() {
        OptionalComplexWithDefaultPresent cfg = ok("return [:]", OptionalComplexWithDefaultPresent.class);
        assertTrue(cfg.onnie.isPresent());
        assertEquals("c", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void optionalComplex_provided_overridesDefault_andMutatesInnerFields() {
        OptionalComplexInnerFieldMutation cfg = ok("return [onnie: [c: 'k']]", OptionalComplexInnerFieldMutation.class);
        assertTrue(cfg.onnie.isPresent());
        assertEquals("k", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void providedNilOptional_isIndistinguishableFromMissing_andKeepsDefaultPresent() {
        OptionalLeafWithDefaultPresent cfg = ok("return [name: null]", OptionalLeafWithDefaultPresent.class);
        assertEquals(Optional.empty(), cfg.name);
    }

    @Test
    void optionalComplex_providedEmptyTable_isPresent_andKeepsInnerDefaults() {
        OptionalComplexNoDefault cfg = ok("return [onnie: [:]]", OptionalComplexNoDefault.class);

        assertTrue(cfg.onnie.isPresent());
        assertEquals("c", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void optionalComplex_providedButInnerCannotInstantiate_fails() {
        ConfigDeserializationException ex = fails("return [bad: [x: 'ok']]", OptionalBadInnerNoNoArg.class);
        assertSingleError(ex, ConfigErrorTypes.NoNoArgCtor.class, "bad");
    }
}
