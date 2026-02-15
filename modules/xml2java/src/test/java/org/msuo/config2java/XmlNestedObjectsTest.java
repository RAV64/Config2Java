package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class XmlNestedObjectsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new XmlDeserializer();
    }


    @Test
    void nestedObject_fromTable_usesNoArgAndSetsFields() {
        CfgNestedPort cfg = ok("<config><db><port>5432</port></db></config>", CfgNestedPort.class);
        assertEquals(Integer.valueOf(5432), cfg.db.port.value);
    }

    @Test
    void emptyTableForComplex_isEnoughWhenFieldsDefaultOrOptional() {
        CfgNestedDefaultsOrOptional cfg = ok("<config><db><dummy/></db></config>", CfgNestedDefaultsOrOptional.class);
        assertEquals("localhost", cfg.db.host.value);
        assertEquals(Optional.empty(), cfg.db.user);
    }

    @Test
    void emptyTableForNestedWithRequiredFields_reportsMissingField() {
        ConfigDeserializationException ex = fails("<config><db><dummy/></db></config>", CfgEmptyTableButNestedHasRequiredField.class);
        assertSingleError(ex, "$.db.port", "Missing required field");
    }

    @Test
    void ifNestedObjectCannotInstantiate_doNotRecurseIntoIt() {
        ConfigDeserializationException ex = fails("<config><bad><x></x></bad></config>", CfgBadNestedNoNoArg.class);
        assertSingleError(ex, "$.bad", "No no-arg constructor");
    }

    @Test
    void nestedObjectProvidedAsPrimitive_failsViaMissingConstructor() {
        ConfigDeserializationException ex = fails("<config><db>nope</db></config>", CfgNestedProvidedAsString.class);
        assertSingleError(ex, "$.db", "No 1-arg constructor");
    }
}
