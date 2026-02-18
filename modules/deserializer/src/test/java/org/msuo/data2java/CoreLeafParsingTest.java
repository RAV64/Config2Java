package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CoreLeafParsingTest extends CoreMapperContractSupport {

    @Test
    void leafString_success() {
        StringLeaf data = ok(obj("name", "ok"), StringLeaf.class);
        assertEquals("ok", data.name.value);
    }

    @Test
    void leafString_invalid_reportsCtorRejected() {
        DataDeserializationException ex = fails(obj("name", ""), StringLeaf.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void leafInteger_success() {
        IntLeaf data = ok(obj("n", 3), IntLeaf.class);
        assertEquals(Integer.valueOf(3), data.n.value);
    }

    @Test
    void leafInteger_invalid_reportsCtorRejected() {
        DataDeserializationException ex = fails(obj("n", 0), IntLeaf.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void enum_success() {
        EnumLeaf data = ok(obj("mode", "PROD"), EnumLeaf.class);
        assertEquals(Mode.PROD, data.mode);
    }

    @Test
    void enum_unknown_reportsEnumUnknown() {
        DataDeserializationException ex = fails(obj("mode", "NOPE"), EnumLeaf.class);
        assertSingleError(ex, DataErrorTypes.EnumUnknown.class, "mode");
    }

    @Test
    void enum_nonString_reportsEnumExpectedString() {
        DataDeserializationException ex = fails(obj("mode", 1), EnumLeaf.class);
        assertSingleError(ex, DataErrorTypes.EnumExpectedString.class, "mode");
    }

    @Test
    void leafString_nonScalar_reportsExpectedScalar() {
        DataDeserializationException ex = fails(
            obj("name", new Object()),
            StringLeaf.class
        );
        assertSingleError(ex, DataErrorTypes.ExpectedScalar.class, "name");
    }
}
