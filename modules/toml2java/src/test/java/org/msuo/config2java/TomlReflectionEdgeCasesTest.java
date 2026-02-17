package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlReflectionEdgeCasesTest extends TomlContractSupport {

    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("base = 'x'\nchild = 1", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("n = 1", PrimitiveFieldNotSupported.class);
        assertSingleError(ex, ConfigErrorTypes.PrimitiveNotSupported.class, "n");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        ConfigSourceException ex = assertThrows(ConfigSourceException.class, () ->
            deserialize("'nope'", RootIsComplex.class)
        );
        assertEquals("TOML", ex.format());
        assertEquals("parse", ex.phase());
    }

    @Test
    void unresolvedTypeVariableField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "value = 'x'",
            UnresolvedTypeVariableField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "value");
    }

    @Test
    void unresolvedTypeVariableArrayField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "values = ['x']",
            UnresolvedTypeVariableArrayField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "values");
    }

    @Test
    void wildcardNestedGenericField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "[foo]\nvalue = 'x'",
            WildcardGenericNestedField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "foo", "value");
    }
}
