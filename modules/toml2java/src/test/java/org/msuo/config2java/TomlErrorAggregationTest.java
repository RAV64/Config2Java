package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlErrorAggregationTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new TomlDeserializer();
    }


    @Test
    void collectAllErrors_continueAfterFailures() {
        ConfigDeserializationException ex = fails("a = ''\nb = 0", CfgCollectAllErrors.class);
        assertEquals(2, ex.getErrors().size());
        assertErrorPaths(ex, "$.a", "$.b");
    }
}
