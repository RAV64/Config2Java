package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CoreMissingDefaultsAndNestedTest extends CoreMapperContractSupport {

    @Test
    void missingRequired_reportsMissingRequiredField() {
        DataDeserializationException ex = fails(obj(), MissingRequired.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "name");
    }

    @Test
    void missingOptional_mapsToEmptyOptional() {
        MissingOptional data = ok(obj(), MissingOptional.class);
        assertEquals(java.util.Optional.empty(), data.name);
    }

    @Test
    void missingWithDefault_keepsDefaultValue() {
        DefaultValue data = ok(obj(), DefaultValue.class);
        assertEquals("default", data.name.value);
    }

    @Test
    void nestedObject_success() {
        NestedPortContainer data = ok(obj("db", obj("port", 5432)), NestedPortContainer.class);
        assertEquals(Integer.valueOf(5432), data.db.port.value);
    }

    @Test
    void nestedEmptyTable_missingRequiredNestedField() {
        DataDeserializationException ex = fails(obj("db", obj()), NestedPortContainer.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "db", "port");
    }

    @Test
    void nestedNoNoArgCtor_reportsNoNoArgCtor() {
        DataDeserializationException ex = fails(
            obj("bad", obj("x", "x")),
            BadNestedNoNoArgContainer.class
        );
        assertSingleError(ex, DataErrorTypes.NoNoArgCtor.class, "bad");
    }
}
