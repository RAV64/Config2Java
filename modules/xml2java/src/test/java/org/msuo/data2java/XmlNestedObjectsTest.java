package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class XmlNestedObjectsTest extends XmlContractSupport {

    @Test
    void nestedObject_fromTable_usesNoArgAndSetsFields() {
        NestedPortContainer data = ok("<data><db><port>5432</port></db></data>", NestedPortContainer.class);
        assertEquals(Integer.valueOf(5432), data.db.port.value);
    }

    @Test
    void emptyTableForComplex_isEnoughWhenFieldsDefaultOrOptional() {
        NestedDefaultsOrOptionalContainer data = ok("<data><db/></data>", NestedDefaultsOrOptionalContainer.class);
        assertEquals("localhost", data.db.host.value);
        assertEquals(Optional.empty(), data.db.user);
    }

    @Test
    void emptyTableForNestedWithRequiredFields_reportsMissingField() {
        DataDeserializationException ex = fails("<data><db/></data>", EmptyTableButNestedHasRequiredField.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "db", "port");
    }

    @Test
    void ifNestedObjectCannotInstantiate_doNotRecurseIntoIt() {
        DataDeserializationException ex = fails("<data><bad><x></x></bad></data>", BadNestedNoNoArg.class);
        assertSingleError(ex, DataErrorTypes.NoNoArgCtor.class, "bad");
    }

    @Test
    void nestedObjectProvidedAsPrimitive_failsViaMissingConstructor() {
        DataDeserializationException ex = fails("<data><db>nope</db></data>", NestedProvidedAsString.class);
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "db");
    }

    @Test
    void nestedObject_withUnknownField_reportsNestedUnknownField() {
        DataDeserializationException ex = fails(
            "<data><db><port>5432</port><extra>1</extra></db></data>",
            NestedPortContainer.class
        );
        assertSingleError(ex, DataErrorTypes.UnknownField.class, "db", "extra");
    }

    @Test
    void structuredTextNode_withoutMatchingField_reportsUnknownField() {
        DataDeserializationException ex = fails(
            "<data><db>text<port>5432</port></db></data>",
            NestedPortContainer.class
        );
        assertSingleError(ex, DataErrorTypes.UnknownField.class, "db", "text");
    }
}
