package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlReflectionEdgeCasesTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new XmlDeserializer();
    }

    @Override
    protected String rootFailureContains() {
        return "Failed to parse XML";
    }


    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("<config><base>x</base><child>1</child></config>", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("<config><n>1</n></config>", CfgPrimitiveFieldNotSupported.class);
        assertSingleError(ex, "$.n", "Primitive field types are not supported");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        Throwable ex = assertThrows(Throwable.class, () ->
            deserializer().deserialize("<config>", CfgRootIsComplex.class)
        );
        assertTrue(ex.getMessage().contains(rootFailureContains()));
    }
}
