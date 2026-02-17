package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class JsonErrorAggregationTest extends JsonContractSupport {

    @Test
    void collectAllErrors_continueAfterFailures() {
        ConfigDeserializationException ex = fails("{\"a\":\"\",\"b\":0}", CollectAllErrors.class);
        assertEquals(2, ex.getErrors().size());
        assertErrorTreeRootChildren(ex, "a", "b");
        assertErrorType(ex, 0, ConfigErrorTypes.CtorRejected.class);
        assertErrorType(ex, 1, ConfigErrorTypes.CtorRejected.class);
    }

    @Test
    void errorTree_containsExpectedRootChildren() {
        ConfigDeserializationException ex = fails("{\"a\":\"\",\"b\":0}", CollectAllErrors.class);
        assertErrorTreeRootChildren(ex, "a", "b");
    }
}
