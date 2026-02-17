package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class JsonReflectionEdgeCasesTest extends JsonContractSupport {

    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("{\"base\":\"x\",\"child\":1}", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("{\"n\":1}", PrimitiveFieldNotSupported.class);
        assertSingleError(ex, ConfigErrorTypes.PrimitiveNotSupported.class, "n");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        ConfigDeserializationException ex = fails("\"nope\"", RootIsComplex.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class);
    }

    @Test
    void unresolvedTypeVariableField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "{\"value\":\"x\"}",
            UnresolvedTypeVariableField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "value");
    }

    @Test
    void unresolvedTypeVariableArrayField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "{\"values\":[\"x\"]}",
            UnresolvedTypeVariableArrayField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "values");
    }

    @Test
    void wildcardNestedGenericField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "{\"foo\":{\"value\":\"x\"}}",
            WildcardGenericNestedField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "foo", "value");
    }
}
