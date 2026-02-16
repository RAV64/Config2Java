package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class LuaReflectionEdgeCasesTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new LuaDeserializer();
    }


    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("return { base = 'x', child = 1 }", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("return { n = 1 }", CfgPrimitiveFieldNotSupported.class);
        assertSingleError(ex, ConfigErrorKind.PrimitiveNotSupported, "n");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        ConfigDeserializationException ex = fails("return 'nope'", CfgRootIsComplex.class);
        assertSingleError(ex, ConfigErrorKind.NoOneArgCtor);
    }
}
