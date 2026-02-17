package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class LuaNestedObjectsTest extends LuaContractSupport {

    @Test
    void nestedObject_fromTable_usesNoArgAndSetsFields() {
        NestedPortContainer cfg = ok("return { db = { port = 5432 } }", NestedPortContainer.class);
        assertEquals(Integer.valueOf(5432), cfg.db.port.value);
    }

    @Test
    void emptyTableForComplex_isEnoughWhenFieldsDefaultOrOptional() {
        NestedDefaultsOrOptionalContainer cfg = ok("return { db = {} }", NestedDefaultsOrOptionalContainer.class);
        assertEquals("localhost", cfg.db.host.value);
        assertEquals(Optional.empty(), cfg.db.user);
    }

    @Test
    void emptyTableForNestedWithRequiredFields_reportsMissingField() {
        ConfigDeserializationException ex = fails("return { db = {} }", EmptyTableButNestedHasRequiredField.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "db", "port");
    }

    @Test
    void ifNestedObjectCannotInstantiate_doNotRecurseIntoIt() {
        ConfigDeserializationException ex = fails("return { bad = { x = '' } }", BadNestedNoNoArg.class);
        assertSingleError(ex, ConfigErrorTypes.NoNoArgCtor.class, "bad");
    }

    @Test
    void nestedObjectProvidedAsPrimitive_failsViaMissingConstructor() {
        ConfigDeserializationException ex = fails("return { db = 'nope' }", NestedProvidedAsString.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "db");
    }
}
