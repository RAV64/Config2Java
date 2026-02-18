package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class GroovyOptionalsTest extends GroovyContractSupport {

    @Test
    void optionalOfComplex_supported() {
        OptionalOfComplex data = ok("return [db: [host: 'x']]", OptionalOfComplex.class);
        assertTrue(data.db.isPresent());
        assertEquals("x", data.db.get().host.value);
    }

    @Test
    void optionalLeaf_badValue_reportsError() {
        DataDeserializationException ex = fails("return [n: 0]", OptionalLeafBadValue.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "n");
    }
}
