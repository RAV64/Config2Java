package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CoreMissingDefaultsAndNestedTest extends CoreMapperContractSupport {

    @Test
    void missingRequired_reportsMissingRequiredField() {
        ConfigDeserializationException ex = fails(obj(), MissingRequired.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "name");
    }

    @Test
    void missingOptional_mapsToEmptyOptional() {
        MissingOptional cfg = ok(obj(), MissingOptional.class);
        assertEquals(java.util.Optional.empty(), cfg.name);
    }

    @Test
    void missingWithDefault_keepsDefaultValue() {
        DefaultValue cfg = ok(obj(), DefaultValue.class);
        assertEquals("default", cfg.name.value);
    }

    @Test
    void nestedObject_success() {
        NestedPortContainer cfg = ok(obj("db", obj("port", 5432)), NestedPortContainer.class);
        assertEquals(Integer.valueOf(5432), cfg.db.port.value);
    }

    @Test
    void nestedEmptyTable_missingRequiredNestedField() {
        ConfigDeserializationException ex = fails(obj("db", obj()), NestedPortContainer.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "db", "port");
    }

    @Test
    void nestedNoNoArgCtor_reportsNoNoArgCtor() {
        ConfigDeserializationException ex = fails(
            obj("bad", obj("x", "x")),
            BadNestedNoNoArgContainer.class
        );
        assertSingleError(ex, ConfigErrorTypes.NoNoArgCtor.class, "bad");
    }
}
