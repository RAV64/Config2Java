package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlOptionalsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new XmlDeserializer();
    }


    @Test
    void optionalOfComplex_supported() {
        CfgOptionalOfComplex cfg = ok("<config><db><host>x</host></db></config>", CfgOptionalOfComplex.class);
        assertTrue(cfg.db.isPresent());
        assertEquals("x", cfg.db.get().host.value);
    }

    @Test
    void optionalLeaf_badValue_reportsError() {
        ConfigDeserializationException ex = fails("<config><n>0</n></config>", CfgOptionalLeafBadValue.class);
        assertSingleError(ex, "$.n", "must be > 0");
    }
}
