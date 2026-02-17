package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlOptionalsTest extends TomlContractSupport {

    @Test
    void optionalOfComplex_supported() {
        OptionalOfComplex cfg = ok("[db]\nhost = 'x'", OptionalOfComplex.class);
        assertTrue(cfg.db.isPresent());
        assertEquals("x", cfg.db.get().host.value);
    }

    @Test
    void optionalLeaf_badValue_reportsError() {
        ConfigDeserializationException ex = fails("n = 0", OptionalLeafBadValue.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "n");
    }
}
