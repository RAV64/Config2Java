package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class LuaErrorAggregationTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new LuaDeserializer();
    }


    @Test
    void collectAllErrors_continueAfterFailures() {
        ConfigDeserializationException ex = fails("return { a = '', b = 0 }", CfgCollectAllErrors.class);
        assertEquals(2, ex.getErrors().size());
        assertErrorPaths(ex, "$.a", "$.b");
    }
}
