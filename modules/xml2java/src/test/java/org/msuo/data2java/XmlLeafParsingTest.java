package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlLeafParsingTest extends XmlContractSupport {

    @Test
    void stringLeaf_usesStringConstructor() {
        StringLeaf data = ok("<data><name>ok</name></data>", StringLeaf.class);
        assertEquals("ok", data.name.value);
    }

    @Test
    void stringLeaf_validationFailure_isReported() {
        DataDeserializationException ex = fails("<data><name></name></data>", StringLeaf.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void intLeaf_usesIntegerConstructor() {
        IntLeaf data = ok("<data><n>3</n></data>", IntLeaf.class);
        assertEquals(Integer.valueOf(3), data.n.value);
    }

    @Test
    void intLeaf_validationFailure_isReported() {
        DataDeserializationException ex = fails("<data><n>0</n></data>", IntLeaf.class);
        assertSingleError(ex, DataErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void float_supported_whenLeafHasDoubleConstructor() {
        DoubleLeaf data = ok("<data><x>1.25</x></data>", DoubleLeaf.class);
        assertEquals(Double.valueOf(1.25), data.x.value);
    }

    @Test
    void integerProvidedToDoubleLeaf_isNotAutoCoerced() {
        DataDeserializationException ex = fails("<data><x>1</x></data>", DoubleLeaf.class);
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "x");
    }

    @Test
    void enum_supported_fromString() {
        Enum data = ok("<data><mode>PROD</mode></data>", Enum.class);
        assertEquals(Mode.PROD, data.mode);
    }

    @Test
    void enum_unknownValue_fails() {
        DataDeserializationException ex = fails("<data><mode>NOPE</mode></data>", Enum.class);
        assertSingleError(ex, DataErrorTypes.EnumUnknown.class, "mode");
    }

    @Test
    void booleanLeaf_supported_onlyForBooleanTarget() {
        BooleanLeaf data = ok("<data><enabled>true</enabled></data>", BooleanLeaf.class);
        assertEquals(Boolean.TRUE, data.enabled);
    }
}
