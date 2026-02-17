package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class XmlMissingAndDefaultsTest extends XmlContractSupport {

    @Test
    void missingRequired_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("<config/>", MissingRequired.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "name");
    }

    @Test
    void missingOptional_defaultsToEmpty() {
        MissingOptional cfg = ok("<config/>", MissingOptional.class);
        assertEquals(Optional.empty(), cfg.name);
    }

    @Test
    void optionalDefaultPresent_isKeptWhenKeyMissing() {
        OptionalHasDefaultPresent cfg = ok("<config/>", OptionalHasDefaultPresent.class);
        assertTrue(cfg.name.isPresent());
        assertEquals("x", cfg.name.get().value);
    }

    @Test
    void defaultValue_isKeptWhenKeyMissing() {
        DefaultValue cfg = ok("<config/>", DefaultValue.class);
        assertEquals("default", cfg.name.value);
    }

    @Test
    void extraKeys_areIgnored() {
        ExtraKeysIgnored cfg = ok("<config><name>ok</name><extra>123</extra><other><a>1</a></other></config>", ExtraKeysIgnored.class);
        assertEquals("ok", cfg.name.value);
    }

    @Test
    void defaultNestedObject_isKeptWhenKeyMissing() {
        DefaultNestedObjectKept cfg = ok("<config/>", DefaultNestedObjectKept.class);
        assertNotNull(cfg.db);
        assertEquals("localhost", cfg.db.host.value);
        assertEquals(Optional.empty(), cfg.db.user);
    }
}
