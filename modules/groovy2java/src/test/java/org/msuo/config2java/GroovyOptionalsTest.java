package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class GroovyOptionalsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new GroovyDeserializer();
    }


    @Test
    void optionalOfComplex_supported() {
        CfgOptionalOfComplex cfg = ok("return [db: [host: 'x']]", CfgOptionalOfComplex.class);
        assertTrue(cfg.db.isPresent());
        assertEquals("x", cfg.db.get().host.value);
    }

    @Test
    void optionalLeaf_badValue_reportsError() {
        ConfigDeserializationException ex = fails("return [n: 0]", CfgOptionalLeafBadValue.class);
        assertSingleError(ex, ConfigErrorKind.CtorRejected, "n");
    }
}
