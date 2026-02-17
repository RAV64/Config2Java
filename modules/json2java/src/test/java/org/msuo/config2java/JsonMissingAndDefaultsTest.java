package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class JsonMissingAndDefaultsTest extends JsonContractSupport {

    @Test
    void missingRequired_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("{}", MissingRequired.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "name");
    }

    @Test
    void missingOptional_defaultsToEmpty() {
        MissingOptional cfg = ok("{}", MissingOptional.class);
        assertEquals(Optional.empty(), cfg.name);
    }

    @Test
    void optionalDefaultPresent_isKeptWhenKeyMissing() {
        OptionalHasDefaultPresent cfg = ok("{}", OptionalHasDefaultPresent.class);
        assertTrue(cfg.name.isPresent());
        assertEquals("x", cfg.name.get().value);
    }

    @Test
    void defaultValue_isKeptWhenKeyMissing() {
        DefaultValue cfg = ok("{}", DefaultValue.class);
        assertEquals("default", cfg.name.value);
    }

    @Test
    void extraKeys_areIgnored() {
        ExtraKeysIgnored cfg = ok("{\"name\":\"ok\",\"extra\":123,\"other\":{\"a\":1}}", ExtraKeysIgnored.class);
        assertEquals("ok", cfg.name.value);
    }

    @Test
    void defaultNestedObject_isKeptWhenKeyMissing() {
        DefaultNestedObjectKept cfg = ok("{}", DefaultNestedObjectKept.class);
        assertNotNull(cfg.db);
        assertEquals("localhost", cfg.db.host.value);
        assertEquals(Optional.empty(), cfg.db.user);
    }
}
