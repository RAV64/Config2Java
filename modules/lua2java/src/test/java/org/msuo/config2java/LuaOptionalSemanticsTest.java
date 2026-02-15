package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class LuaOptionalSemanticsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new LuaDeserializer();
    }


    @Test
    void missingOptional_withoutDefault_defaultsToEmpty() {
        CfgOptionalLeafNoDefault cfg = ok("return {}", CfgOptionalLeafNoDefault.class);
        assertEquals(Optional.empty(), cfg.n);
    }

    @Test
    void optionalComplex_nilOrMissing_keepsDefaultPresent() {
        CfgOptionalComplexWithDefaultPresent cfg = ok(
            "return { onnie = nil }",
            CfgOptionalComplexWithDefaultPresent.class
        );
        assertTrue(cfg.onnie.isPresent());
        assertEquals("c", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void missingOptional_keepsDefaultPresent() {
        CfgOptionalLeafWithDefaultPresent cfg = ok("return {}", CfgOptionalLeafWithDefaultPresent.class);
        assertTrue(cfg.name.isPresent());
        assertEquals("default", cfg.name.get().value);
    }

    @Test
    void missingOptional_keepsDefaultEmpty() {
        CfgOptionalLeafWithDefaultEmpty cfg = ok("return {}", CfgOptionalLeafWithDefaultEmpty.class);
        assertEquals(Optional.empty(), cfg.name);
    }

    @Test
    void providedValidOptionalLeaf_parsesPresent() {
        CfgOptionalLeafNoDefault cfg = ok("return { n = 3 }", CfgOptionalLeafNoDefault.class);
        assertTrue(cfg.n.isPresent());
        assertEquals(Integer.valueOf(3), cfg.n.get().value);
    }

    @Test
    void providedInvalidOptionalLeaf_fails_andDoesNotOverwriteDefaultPresent() {
        ConfigDeserializationException ex = fails("return { name = '' }", CfgOptionalLeafWithDefaultPresent.class);
        assertSingleError(ex, "$.name", "must be non-empty");
    }

    @Test
    void providedInvalidOptionalLeaf_fails_atOptionalPath() {
        ConfigDeserializationException ex = fails("return { n = 0 }", CfgOptionalLeafNoDefault.class);
        assertSingleError(ex, "$.n", "must be > 0");
    }

    @Test
    void optionalComplex_missing_withoutDefault_defaultsToEmpty() {
        CfgOptionalComplexNoDefault cfg = ok("return {}", CfgOptionalComplexNoDefault.class);
        assertEquals(Optional.empty(), cfg.onnie);
    }

    @Test
    void optionalComplex_missing_keepsDefaultPresent() {
        CfgOptionalComplexWithDefaultPresent cfg = ok("return {}", CfgOptionalComplexWithDefaultPresent.class);
        assertTrue(cfg.onnie.isPresent());
        assertEquals("c", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void optionalComplex_provided_overridesDefault_andMutatesInnerFields() {
        CfgOptionalComplexInnerFieldMutation cfg = ok("return { onnie = { c = 'k' } }", CfgOptionalComplexInnerFieldMutation.class);
        assertTrue(cfg.onnie.isPresent());
        assertEquals("k", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void providedNilOptional_isIndistinguishableFromMissing_andKeepsDefaultPresent() {
        CfgOptionalLeafWithDefaultPresent cfg = ok("return { name = nil }", CfgOptionalLeafWithDefaultPresent.class);
        assertTrue(cfg.name.isPresent());
        assertEquals("default", cfg.name.get().value);
    }

    @Test
    void optionalComplex_providedEmptyTable_isPresent_andKeepsInnerDefaults() {
        CfgOptionalComplexNoDefault cfg = ok("return { onnie = {} }", CfgOptionalComplexNoDefault.class);

        assertTrue(cfg.onnie.isPresent());
        assertEquals("c", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void optionalComplex_providedButInnerCannotInstantiate_fails() {
        ConfigDeserializationException ex = fails("return { bad = { x = 'ok' } }", CfgOptionalBadInnerNoNoArg.class);
        assertSingleError(ex, "$.bad", "No no-arg constructor");
    }
}
