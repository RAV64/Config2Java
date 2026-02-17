package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class XmlNestedObjectsTest extends XmlContractSupport {

    @Test
    void nestedObject_fromTable_usesNoArgAndSetsFields() {
        NestedPortContainer cfg = ok("<config><db><port>5432</port></db></config>", NestedPortContainer.class);
        assertEquals(Integer.valueOf(5432), cfg.db.port.value);
    }

    @Test
    void emptyTableForComplex_isEnoughWhenFieldsDefaultOrOptional() {
        NestedDefaultsOrOptionalContainer cfg = ok("<config><db><dummy/></db></config>", NestedDefaultsOrOptionalContainer.class);
        assertEquals("localhost", cfg.db.host.value);
        assertEquals(Optional.empty(), cfg.db.user);
    }

    @Test
    void emptyTableForNestedWithRequiredFields_reportsMissingField() {
        ConfigDeserializationException ex = fails("<config><db><dummy/></db></config>", EmptyTableButNestedHasRequiredField.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "db", "port");
    }

    @Test
    void ifNestedObjectCannotInstantiate_doNotRecurseIntoIt() {
        ConfigDeserializationException ex = fails("<config><bad><x></x></bad></config>", BadNestedNoNoArg.class);
        assertSingleError(ex, ConfigErrorTypes.NoNoArgCtor.class, "bad");
    }

    @Test
    void nestedObjectProvidedAsPrimitive_failsViaMissingConstructor() {
        ConfigDeserializationException ex = fails("<config><db>nope</db></config>", NestedProvidedAsString.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class, "db");
    }
}
