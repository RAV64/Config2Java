package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class XmlMissingAndDefaultsTest extends XmlContractSupport {

    @Test
    void missingRequired_withoutDefault_fails() {
        DataDeserializationException ex = fails("<data/>", MissingRequired.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "name");
    }

    @Test
    void missingOptional_defaultsToEmpty() {
        MissingOptional data = ok("<data/>", MissingOptional.class);
        assertEquals(Optional.empty(), data.name);
    }

    @Test
    void optionalDefaultPresent_isKeptWhenKeyMissing() {
        OptionalHasDefaultPresent data = ok("<data/>", OptionalHasDefaultPresent.class);
        assertTrue(data.name.isPresent());
        assertEquals("x", data.name.get().value);
    }

    @Test
    void defaultValue_isKeptWhenKeyMissing() {
        DefaultValue data = ok("<data/>", DefaultValue.class);
        assertEquals("default", data.name.value);
    }

    @Test
    void extraKeys_reportUnknownFieldErrors() {
        DataDeserializationException ex = fails(
            "<data><name>ok</name><extra>123</extra><other><a>1</a></other></data>",
            UnknownFieldInput.class
        );
        assertEquals(2, ex.getErrors().size());
        assertHasError(ex, DataErrorTypes.UnknownField.class, "extra");
        assertHasError(ex, DataErrorTypes.UnknownField.class, "other");
    }

    @Test
    void mixedKnownFailureAndUnknownField_areBothReported() {
        DataDeserializationException ex = fails(
            "<data><name></name><extra>1</extra></data>",
            UnknownFieldInput.class
        );
        assertEquals(2, ex.getErrors().size());
        assertHasError(ex, DataErrorTypes.CtorRejected.class, "name");
        assertHasError(ex, DataErrorTypes.UnknownField.class, "extra");
    }

    @Test
    void inheritedFieldsPlusUnknownField_reportsOnlyUnknownField() {
        DataDeserializationException ex = fails(
            "<data><base>x</base><child>1</child><extra>1</extra></data>",
            DerivedData.class
        );
        assertSingleError(ex, DataErrorTypes.UnknownField.class, "extra");
    }

    @Test
    void emptyModel_withInputKeys_reportsUnknownFields() {
        DataDeserializationException ex = fails(
            "<data><a>1</a><b>2</b></data>",
            EmptyData.class
        );
        assertEquals(2, ex.getErrors().size());
        assertHasError(ex, DataErrorTypes.UnknownField.class, "a");
        assertHasError(ex, DataErrorTypes.UnknownField.class, "b");
    }

    @Test
    void defaultNestedObject_isKeptWhenKeyMissing() {
        DefaultNestedObjectKept data = ok("<data/>", DefaultNestedObjectKept.class);
        assertNotNull(data.db);
        assertEquals("localhost", data.db.host.value);
        assertEquals(Optional.empty(), data.db.user);
    }
}
