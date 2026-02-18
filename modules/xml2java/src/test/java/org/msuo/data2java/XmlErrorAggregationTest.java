package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlErrorAggregationTest extends XmlContractSupport {

    @Test
    void collectAllErrors_continueAfterFailures() {
        DataDeserializationException ex = fails("<data><a></a><b>0</b></data>", CollectAllErrors.class);
        assertEquals(2, ex.getErrors().size());
        assertErrorTreeRootChildren(ex, "a", "b");
        assertErrorType(ex, 0, DataErrorTypes.CtorRejected.class);
        assertErrorType(ex, 1, DataErrorTypes.CtorRejected.class);
    }

    @Test
    void errorTree_containsExpectedRootChildren() {
        DataDeserializationException ex = fails("<data><a></a><b>0</b></data>", CollectAllErrors.class);
        assertErrorTreeRootChildren(ex, "a", "b");
    }
}
