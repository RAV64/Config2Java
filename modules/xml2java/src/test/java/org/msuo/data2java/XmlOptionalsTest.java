package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlOptionalsTest extends XmlContractSupport {

    @Test
    void optionalOfComplex_supported() {
        OptionalOfComplex data = ok("<data><db><host>x</host></db></data>", OptionalOfComplex.class);
        assertTrue(data.db.isPresent());
        assertEquals("x", data.db.get().host.value);
    }

    @Test
    void optionalLeaf_badValue_reportsError() {
        DataDeserializationException ex = fails("<data><n>0</n></data>", OptionalLeafBadValue.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "n");
    }
}
