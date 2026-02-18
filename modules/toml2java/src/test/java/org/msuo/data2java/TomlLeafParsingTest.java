package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlLeafParsingTest extends TomlContractSupport {

    @Test
    void stringLeaf_usesStringConstructor() {
        StringLeaf data = ok("name = 'ok'", StringLeaf.class);
        assertEquals("ok", data.name.value);
    }

    @Test
    void stringLeaf_validationFailure_isReported() {
        DataDeserializationException ex = fails("name = ''", StringLeaf.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void intLeaf_usesIntegerConstructor() {
        IntLeaf data = ok("n = 3", IntLeaf.class);
        assertEquals(Integer.valueOf(3), data.n.value);
    }

    @Test
    void intLeaf_validationFailure_isReported() {
        DataDeserializationException ex = fails("n = 0", IntLeaf.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void float_supported_whenLeafHasDoubleConstructor() {
        DoubleLeaf data = ok("x = 1.25", DoubleLeaf.class);
        assertEquals(Double.valueOf(1.25), data.x.value);
    }

    @Test
    void integerProvidedToDoubleLeaf_isNotAutoCoerced() {
        DataDeserializationException ex = fails("x = 1", DoubleLeaf.class);
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "x");
    }

    @Test
    void enum_supported_fromString() {
        Enum data = ok("mode = 'PROD'", Enum.class);
        assertEquals(Mode.PROD, data.mode);
    }

    @Test
    void enum_unknownValue_fails() {
        DataDeserializationException ex = fails("mode = 'NOPE'", Enum.class);
        assertSingleError(ex, DataErrorTypes.EnumUnknown.class, "mode");
    }

    @Test
    void booleanLeaf_supported_onlyForBooleanTarget() {
        BooleanLeaf data = ok("enabled = true", BooleanLeaf.class);
        assertEquals(Boolean.TRUE, data.enabled);
    }
}
