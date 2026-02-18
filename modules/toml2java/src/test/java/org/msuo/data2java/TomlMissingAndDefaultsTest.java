package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class TomlMissingAndDefaultsTest extends TomlContractSupport {

    @Test
    void missingRequired_withoutDefault_fails() {
        DataDeserializationException ex = fails("", MissingRequired.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "name");
    }

    @Test
    void missingOptional_defaultsToEmpty() {
        MissingOptional data = ok("", MissingOptional.class);
        assertEquals(Optional.empty(), data.name);
    }

    @Test
    void optionalDefaultPresent_isKeptWhenKeyMissing() {
        OptionalHasDefaultPresent data = ok("", OptionalHasDefaultPresent.class);
        assertTrue(data.name.isPresent());
        assertEquals("x", data.name.get().value);
    }

    @Test
    void defaultValue_isKeptWhenKeyMissing() {
        DefaultValue data = ok("", DefaultValue.class);
        assertEquals("default", data.name.value);
    }

    @Test
    void extraKeys_reportUnknownFieldErrors() {
        DataDeserializationException ex = fails(
            "name = 'ok'\nextra = 123\n[other]\na = 1",
            UnknownFieldInput.class
        );
        assertEquals(2, ex.getErrors().size());
        assertHasError(ex, DataErrorTypes.UnknownField.class, "extra");
        assertHasError(ex, DataErrorTypes.UnknownField.class, "other");
    }

    @Test
    void mixedKnownFailureAndUnknownField_areBothReported() {
        DataDeserializationException ex = fails(
            "name = ''\nextra = 1",
            UnknownFieldInput.class
        );
        assertEquals(2, ex.getErrors().size());
        assertHasError(ex, DataErrorTypes.CtorRejected.class, "name");
        assertHasError(ex, DataErrorTypes.UnknownField.class, "extra");
    }

    @Test
    void inheritedFieldsPlusUnknownField_reportsOnlyUnknownField() {
        DataDeserializationException ex = fails(
            "base = 'x'\nchild = 1\nextra = 1",
            DerivedData.class
        );
        assertSingleError(ex, DataErrorTypes.UnknownField.class, "extra");
    }

    @Test
    void emptyModel_withInputKeys_reportsUnknownFields() {
        DataDeserializationException ex = fails(
            "a = 1\nb = 2",
            EmptyData.class
        );
        assertEquals(2, ex.getErrors().size());
        assertHasError(ex, DataErrorTypes.UnknownField.class, "a");
        assertHasError(ex, DataErrorTypes.UnknownField.class, "b");
    }

    @Test
    void defaultNestedObject_isKeptWhenKeyMissing() {
        DefaultNestedObjectKept data = ok("", DefaultNestedObjectKept.class);
        assertNotNull(data.db);
        assertEquals("localhost", data.db.host.value);
        assertEquals(Optional.empty(), data.db.user);
    }
}
