package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlOptionalsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new TomlDeserializer();
    }


    @Test
    void optionalOfComplex_supported() {
        CfgOptionalOfComplex cfg = ok("[db]\nhost = 'x'", CfgOptionalOfComplex.class);
        assertTrue(cfg.db.isPresent());
        assertEquals("x", cfg.db.get().host.value);
    }

    @Test
    void optionalLeaf_badValue_reportsError() {
        ConfigDeserializationException ex = fails("n = 0", CfgOptionalLeafBadValue.class);
        assertSingleError(ex, "$.n", "must be > 0");
    }
}
