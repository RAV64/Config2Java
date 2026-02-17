package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlLeafParsingTest extends XmlContractSupport {

    @Test
    void stringLeaf_usesStringConstructor() {
        StringLeaf cfg = ok("<config><name>ok</name></config>", StringLeaf.class);
        assertEquals("ok", cfg.name.value);
    }

    @Test
    void stringLeaf_validationFailure_isReported() {
        ConfigDeserializationException ex = fails("<config><name></name></config>", StringLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void intLeaf_usesIntegerConstructor() {
        IntLeaf cfg = ok("<config><n>3</n></config>", IntLeaf.class);
        assertEquals(Integer.valueOf(3), cfg.n.value);
    }

    @Test
    void intLeaf_validationFailure_isReported() {
        ConfigDeserializationException ex = fails("<config><n>0</n></config>", IntLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void float_supported_whenLeafHasDoubleConstructor() {
        DoubleLeaf cfg = ok("<config><x>1.25</x></config>", DoubleLeaf.class);
        assertEquals(Double.valueOf(1.25), cfg.x.value);
    }

    @Test
    void integerProvidedToDoubleLeaf_isNotAutoCoerced() {
        ConfigDeserializationException ex = fails("<config><x>1</x></config>", DoubleLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "x");
    }

    @Test
    void enum_supported_fromString() {
        Enum cfg = ok("<config><mode>PROD</mode></config>", Enum.class);
        assertEquals(Mode.PROD, cfg.mode);
    }

    @Test
    void enum_unknownValue_fails() {
        ConfigDeserializationException ex = fails("<config><mode>NOPE</mode></config>", Enum.class);
        assertSingleError(ex, ConfigErrorTypes.EnumUnknown.class, "mode");
    }

    @Test
    void booleanLeaf_supported_onlyForBooleanTarget() {
        BooleanLeaf cfg = ok("<config><enabled>true</enabled></config>", BooleanLeaf.class);
        assertEquals(Boolean.TRUE, cfg.enabled);
    }
}
