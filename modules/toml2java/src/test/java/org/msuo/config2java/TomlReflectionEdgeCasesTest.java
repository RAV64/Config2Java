package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlReflectionEdgeCasesTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new TomlDeserializer();
    }

    @Override
    protected String rootFailureContains() {
        return "Failed to parse TOML";
    }


    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("base = 'x'\nchild = 1", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("n = 1", CfgPrimitiveFieldNotSupported.class);
        assertSingleError(ex, "$.n", "Primitive field types are not supported");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        Throwable ex = assertThrows(Throwable.class, () ->
            deserializer().deserialize("'nope'", CfgRootIsComplex.class)
        );
        assertTrue(ex.getMessage().contains(rootFailureContains()));
    }
}
