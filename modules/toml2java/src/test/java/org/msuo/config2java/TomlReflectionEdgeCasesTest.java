package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlReflectionEdgeCasesTest extends TomlContractSupport {

    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("base = 'x'\nchild = 1", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("n = 1", CfgPrimitiveFieldNotSupported.class);
        assertSingleError(ex, ConfigErrorTypes.PrimitiveNotSupported.class, "n");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        ConfigSourceException ex = assertThrows(ConfigSourceException.class, () ->
            deserialize("'nope'", CfgRootIsComplex.class)
        );
        assertEquals("TOML", ex.format());
        assertEquals("parse", ex.phase());
    }
}
