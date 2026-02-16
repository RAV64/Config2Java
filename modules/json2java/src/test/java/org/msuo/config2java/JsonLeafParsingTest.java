package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class JsonLeafParsingTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new JsonDeserializer();
    }


    @Test
    void stringLeaf_usesStringConstructor() {
        CfgStringLeaf cfg = ok("{\"name\":\"ok\"}", CfgStringLeaf.class);
        assertEquals("ok", cfg.name.value);
    }

    @Test
    void stringLeaf_validationFailure_isReported() {
        ConfigDeserializationException ex = fails("{\"name\":\"\"}", CfgStringLeaf.class);
        assertSingleError(ex, ConfigErrorKind.CtorRejected, "name");
    }

    @Test
    void intLeaf_usesIntegerConstructor() {
        CfgIntLeaf cfg = ok("{\"n\":3}", CfgIntLeaf.class);
        assertEquals(Integer.valueOf(3), cfg.n.value);
    }

    @Test
    void intLeaf_validationFailure_isReported() {
        ConfigDeserializationException ex = fails("{\"n\":0}", CfgIntLeaf.class);
        assertSingleError(ex, ConfigErrorKind.CtorRejected, "n");
    }

    @Test
    void float_supported_whenLeafHasDoubleConstructor() {
        CfgDoubleLeaf cfg = ok("{\"x\":1.25}", CfgDoubleLeaf.class);
        assertEquals(Double.valueOf(1.25), cfg.x.value);
    }

    @Test
    void integerProvidedToDoubleLeaf_isNotAutoCoerced() {
        ConfigDeserializationException ex = fails("{\"x\":1}", CfgDoubleLeaf.class);
        assertSingleError(ex, ConfigErrorKind.NoOneArgCtor, "x");
    }

    @Test
    void enum_supported_fromString() {
        CfgEnum cfg = ok("{\"mode\":\"PROD\"}", CfgEnum.class);
        assertEquals(Mode.PROD, cfg.mode);
    }

    @Test
    void enum_unknownValue_fails() {
        ConfigDeserializationException ex = fails("{\"mode\":\"NOPE\"}", CfgEnum.class);
        assertSingleError(ex, ConfigErrorKind.EnumUnknown, "mode");
    }

    @Test
    void booleanLeaf_supported_onlyForBooleanTarget() {
        CfgBooleanLeaf cfg = ok("{\"enabled\":true}", CfgBooleanLeaf.class);
        assertEquals(Boolean.TRUE, cfg.enabled);
    }
}
