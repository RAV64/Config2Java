package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class GroovyReflectionEdgeCasesTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new GroovyDeserializer();
    }


    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("return [base: 'x', child: 1]", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("return [n: 1]", CfgPrimitiveFieldNotSupported.class);
        assertSingleError(ex, "$.n", "Primitive field types are not supported");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        Throwable ex = assertThrows(Throwable.class, () ->
            deserializer().deserialize("return 'nope'", CfgRootIsComplex.class)
        );
        assertTrue(ex.getMessage().contains(rootFailureContains()));
    }
}
