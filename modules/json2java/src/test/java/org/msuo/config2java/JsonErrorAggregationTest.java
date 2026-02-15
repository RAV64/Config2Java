package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class JsonErrorAggregationTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new JsonDeserializer();
    }


    @Test
    void collectAllErrors_continueAfterFailures() {
        ConfigDeserializationException ex = fails("{\"a\":\"\",\"b\":0}", CfgCollectAllErrors.class);
        assertEquals(2, ex.getErrors().size());
        assertErrorPaths(ex, "$.a", "$.b");
    }
}
