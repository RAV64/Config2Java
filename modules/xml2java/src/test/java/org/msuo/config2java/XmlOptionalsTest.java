package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlOptionalsTest extends XmlContractSupport {

    @Test
    void optionalOfComplex_supported() {
        OptionalOfComplex cfg = ok("<config><db><host>x</host></db></config>", OptionalOfComplex.class);
        assertTrue(cfg.db.isPresent());
        assertEquals("x", cfg.db.get().host.value);
    }

    @Test
    void optionalLeaf_badValue_reportsError() {
        ConfigDeserializationException ex = fails("<config><n>0</n></config>", OptionalLeafBadValue.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "n");
    }
}
