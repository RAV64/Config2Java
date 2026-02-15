package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class GroovyErrorAggregationTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new GroovyDeserializer();
    }


    @Test
    void collectAllErrors_continueAfterFailures() {
        ConfigDeserializationException ex = fails("return [a: '', b: 0]", CfgCollectAllErrors.class);
        assertEquals(2, ex.getErrors().size());
        assertErrorPaths(ex, "$.a", "$.b");
    }
}
