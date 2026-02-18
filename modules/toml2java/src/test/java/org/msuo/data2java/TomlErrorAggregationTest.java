package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlErrorAggregationTest extends TomlContractSupport {

    @Test
    void collectAllErrors_continueAfterFailures() {
        DataDeserializationException ex = fails("a = ''\nb = 0", CollectAllErrors.class);
        assertEquals(2, ex.getErrors().size());
        assertErrorTreeRootChildren(ex, "a", "b");
        assertErrorType(ex, 0, DataErrorTypes.CtorRejected.class);
        assertErrorType(ex, 1, DataErrorTypes.CtorRejected.class);
    }

    @Test
    void errorTree_containsExpectedRootChildren() {
        DataDeserializationException ex = fails("a = ''\nb = 0", CollectAllErrors.class);
        assertErrorTreeRootChildren(ex, "a", "b");
    }
}
