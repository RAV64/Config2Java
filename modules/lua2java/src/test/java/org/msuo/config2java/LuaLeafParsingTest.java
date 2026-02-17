package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class LuaLeafParsingTest extends LuaContractSupport {

    @Test
    void stringLeaf_usesStringConstructor() {
        StringLeaf cfg = ok("return { name = 'ok' }", StringLeaf.class);
        assertEquals("ok", cfg.name.value);
    }

    @Test
    void stringLeaf_validationFailure_isReported() {
        ConfigDeserializationException ex = fails("return { name = '' }", StringLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void intLeaf_usesIntegerConstructor() {
        IntLeaf cfg = ok("return { n = 3 }", IntLeaf.class);
        assertEquals(Integer.valueOf(3), cfg.n.value);
    }

    @Test
    void intLeaf_validationFailure_isReported() {
        ConfigDeserializationException ex = fails("return { n = 0 }", IntLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void float_supported_whenLeafHasDoubleConstructor() {
        DoubleLeaf cfg = ok("return { x = 1.25 }", DoubleLeaf.class);
        assertEquals(Double.valueOf(1.25), cfg.x.value);
    }

    @Test
    void integerProvidedToDoubleLeaf_isNotAutoCoerced() {
        ConfigDeserializationException ex = fails("return { x = 1 }", DoubleLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "x");
    }

    @Test
    void enum_supported_fromString() {
        Enum cfg = ok("return { mode = 'PROD' }", Enum.class);
        assertEquals(Mode.PROD, cfg.mode);
    }

    @Test
    void enum_unknownValue_fails() {
        ConfigDeserializationException ex = fails("return { mode = 'NOPE' }", Enum.class);
        assertSingleError(ex, ConfigErrorTypes.EnumUnknown.class, "mode");
    }

    @Test
    void booleanLeaf_supported_onlyForBooleanTarget() {
        BooleanLeaf cfg = ok("return { enabled = true }", BooleanLeaf.class);
        assertEquals(Boolean.TRUE, cfg.enabled);
    }
}
