package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlReflectionEdgeCasesTest extends XmlContractSupport {

    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("<config><base>x</base><child>1</child></config>", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("<config><n>1</n></config>", PrimitiveFieldNotSupported.class);
        assertSingleError(ex, ConfigErrorTypes.PrimitiveNotSupported.class, "n");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        ConfigSourceException ex = assertThrows(ConfigSourceException.class, () ->
            deserialize("<config>", RootIsComplex.class)
        );
        assertEquals("XML", ex.format());
        assertEquals("parse", ex.phase());
    }

    @Test
    void unresolvedTypeVariableField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "<config><value>x</value></config>",
            UnresolvedTypeVariableField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "value");
    }

    @Test
    void unresolvedTypeVariableArrayField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "<config><values>x</values></config>",
            UnresolvedTypeVariableArrayField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "values");
    }

    @Test
    void wildcardNestedGenericField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "<config><foo><value>x</value></foo></config>",
            WildcardGenericNestedField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "foo", "value");
    }
}
