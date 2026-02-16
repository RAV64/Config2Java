package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class XmlOptionalSemanticsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new XmlDeserializer();
    }


    @Test
    void missingOptional_withoutDefault_defaultsToEmpty() {
        CfgOptionalLeafNoDefault cfg = ok("<config/>", CfgOptionalLeafNoDefault.class);
        assertEquals(Optional.empty(), cfg.n);
    }

    @Test
    void optionalComplex_nilOrMissing_keepsDefaultPresent() {
        CfgOptionalComplexWithDefaultPresent cfg = ok(
            "<config/>",
            CfgOptionalComplexWithDefaultPresent.class
        );
        assertTrue(cfg.onnie.isPresent());
        assertEquals("c", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void missingOptional_keepsDefaultPresent() {
        CfgOptionalLeafWithDefaultPresent cfg = ok("<config/>", CfgOptionalLeafWithDefaultPresent.class);
        assertTrue(cfg.name.isPresent());
        assertEquals("default", cfg.name.get().value);
    }

    @Test
    void missingOptional_keepsDefaultEmpty() {
        CfgOptionalLeafWithDefaultEmpty cfg = ok("<config/>", CfgOptionalLeafWithDefaultEmpty.class);
        assertEquals(Optional.empty(), cfg.name);
    }

    @Test
    void providedValidOptionalLeaf_parsesPresent() {
        CfgOptionalLeafNoDefault cfg = ok("<config><n>3</n></config>", CfgOptionalLeafNoDefault.class);
        assertTrue(cfg.n.isPresent());
        assertEquals(Integer.valueOf(3), cfg.n.get().value);
    }

    @Test
    void providedInvalidOptionalLeaf_fails_andDoesNotOverwriteDefaultPresent() {
        ConfigDeserializationException ex = fails("<config><name></name></config>", CfgOptionalLeafWithDefaultPresent.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void providedInvalidOptionalLeaf_fails_atOptionalPath() {
        ConfigDeserializationException ex = fails("<config><n>0</n></config>", CfgOptionalLeafNoDefault.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void optionalComplex_missing_withoutDefault_defaultsToEmpty() {
        CfgOptionalComplexNoDefault cfg = ok("<config/>", CfgOptionalComplexNoDefault.class);
        assertEquals(Optional.empty(), cfg.onnie);
    }

    @Test
    void optionalComplex_missing_keepsDefaultPresent() {
        CfgOptionalComplexWithDefaultPresent cfg = ok("<config/>", CfgOptionalComplexWithDefaultPresent.class);
        assertTrue(cfg.onnie.isPresent());
        assertEquals("c", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void optionalComplex_provided_overridesDefault_andMutatesInnerFields() {
        CfgOptionalComplexInnerFieldMutation cfg = ok("<config><onnie><c>k</c></onnie></config>", CfgOptionalComplexInnerFieldMutation.class);
        assertTrue(cfg.onnie.isPresent());
        assertEquals("k", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void providedNilOptional_isIndistinguishableFromMissing_andKeepsDefaultPresent() {
        CfgOptionalLeafWithDefaultPresent cfg = ok("<config/>", CfgOptionalLeafWithDefaultPresent.class);
        assertTrue(cfg.name.isPresent());
        assertEquals("default", cfg.name.get().value);
    }

    @Test
    void optionalComplex_providedEmptyTable_isPresent_andKeepsInnerDefaults() {
        CfgOptionalComplexNoDefault cfg = ok("<config><onnie><dummy/></onnie></config>", CfgOptionalComplexNoDefault.class);

        assertTrue(cfg.onnie.isPresent());
        assertEquals("c", cfg.onnie.get().c);
        assertEquals(Integer.valueOf(5), cfg.onnie.get().d);
    }

    @Test
    void optionalComplex_providedButInnerCannotInstantiate_fails() {
        ConfigDeserializationException ex = fails("<config><bad><x>ok</x></bad></config>", CfgOptionalBadInnerNoNoArg.class);
        assertSingleError(ex, ConfigErrorTypes.NoNoArgCtor.class, "bad");
    }
}
