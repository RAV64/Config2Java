package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class JsonOptionalsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new JsonDeserializer();
    }


    @Test
    void optionalOfComplex_supported() {
        CfgOptionalOfComplex cfg = ok("{\"db\":{\"host\":\"x\"}}", CfgOptionalOfComplex.class);
        assertTrue(cfg.db.isPresent());
        assertEquals("x", cfg.db.get().host.value);
    }

    @Test
    void optionalLeaf_badValue_reportsError() {
        ConfigDeserializationException ex = fails("{\"n\":0}", CfgOptionalLeafBadValue.class);
        assertSingleError(ex, "$.n", "must be > 0");
    }
}
