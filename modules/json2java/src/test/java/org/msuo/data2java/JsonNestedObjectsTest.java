package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class JsonNestedObjectsTest extends JsonContractSupport {

    @Test
    void nestedObject_fromTable_usesNoArgAndSetsFields() {
        NestedPortContainer data = ok("{\"db\":{\"port\":5432}}", NestedPortContainer.class);
        assertEquals(Integer.valueOf(5432), data.db.port.value);
    }

    @Test
    void emptyTableForComplex_isEnoughWhenFieldsDefaultOrOptional() {
        NestedDefaultsOrOptionalContainer data = ok("{\"db\":{}}", NestedDefaultsOrOptionalContainer.class);
        assertEquals("localhost", data.db.host.value);
        assertEquals(Optional.empty(), data.db.user);
    }

    @Test
    void emptyTableForNestedWithRequiredFields_reportsMissingField() {
        DataDeserializationException ex = fails("{\"db\":{}}", EmptyTableButNestedHasRequiredField.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "db", "port");
    }

    @Test
    void ifNestedObjectCannotInstantiate_doNotRecurseIntoIt() {
        DataDeserializationException ex = fails("{\"bad\":{\"x\":\"\"}}", BadNestedNoNoArg.class);
        assertSingleError(ex, DataErrorTypes.NoNoArgCtor.class, "bad");
    }

    @Test
    void nestedObjectProvidedAsPrimitive_failsViaMissingConstructor() {
        DataDeserializationException ex = fails("{\"db\":\"nope\"}", NestedProvidedAsString.class);
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class, "db");
    }

    @Test
    void nestedObject_withUnknownField_reportsNestedUnknownField() {
        DataDeserializationException ex = fails(
            "{\"db\":{\"port\":5432,\"extra\":1}}",
            NestedPortContainer.class
        );
        assertSingleError(ex, DataErrorTypes.UnknownField.class, "db", "extra");
    }
}
