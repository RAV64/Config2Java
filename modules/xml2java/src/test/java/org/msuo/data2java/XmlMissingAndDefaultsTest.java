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
    void extraKeys_areIgnored() {
        ExtraKeysIgnored data = ok("<data><name>ok</name><extra>123</extra><other><a>1</a></other></data>", ExtraKeysIgnored.class);
        assertEquals("ok", data.name.value);
    }

    @Test
    void defaultNestedObject_isKeptWhenKeyMissing() {
        DefaultNestedObjectKept data = ok("<data/>", DefaultNestedObjectKept.class);
        assertNotNull(data.db);
        assertEquals("localhost", data.db.host.value);
        assertEquals(Optional.empty(), data.db.user);
    }
}
