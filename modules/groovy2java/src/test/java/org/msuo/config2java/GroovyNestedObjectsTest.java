package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class GroovyNestedObjectsTest extends GroovyContractSupport {

    @Test
    void nestedObject_fromTable_usesNoArgAndSetsFields() {
        CfgNestedPort cfg = ok("return [db: [port: 5432]]", CfgNestedPort.class);
        assertEquals(Integer.valueOf(5432), cfg.db.port.value);
    }

    @Test
    void emptyTableForComplex_isEnoughWhenFieldsDefaultOrOptional() {
        CfgNestedDefaultsOrOptional cfg = ok("return [db: [:]]", CfgNestedDefaultsOrOptional.class);
        assertEquals("localhost", cfg.db.host.value);
        assertEquals(Optional.empty(), cfg.db.user);
    }

    @Test
    void emptyTableForNestedWithRequiredFields_reportsMissingField() {
        ConfigDeserializationException ex = fails("return [db: [:]]", CfgEmptyTableButNestedHasRequiredField.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "db", "port");
    }

    @Test
    void ifNestedObjectCannotInstantiate_doNotRecurseIntoIt() {
        ConfigDeserializationException ex = fails("return [bad: [x: '']]", CfgBadNestedNoNoArg.class);
        assertSingleError(ex, ConfigErrorTypes.NoNoArgCtor.class, "bad");
    }

    @Test
    void nestedObjectProvidedAsPrimitive_failsViaMissingConstructor() {
        ConfigDeserializationException ex = fails("return [db: 'nope']", CfgNestedProvidedAsString.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "db");
    }
}
