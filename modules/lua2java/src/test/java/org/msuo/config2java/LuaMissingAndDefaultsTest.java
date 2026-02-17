package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class LuaMissingAndDefaultsTest extends LuaContractSupport {

    @Test
    void missingRequired_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("return {}", MissingRequired.class);
        assertSingleError(ex, ConfigErrorTypes.MissingRequiredField.class, "name");
    }

    @Test
    void missingOptional_defaultsToEmpty() {
        MissingOptional cfg = ok("return {}", MissingOptional.class);
        assertEquals(Optional.empty(), cfg.name);
    }

    @Test
    void optionalDefaultPresent_isKeptWhenKeyMissing() {
        OptionalHasDefaultPresent cfg = ok("return {}", OptionalHasDefaultPresent.class);
        assertTrue(cfg.name.isPresent());
        assertEquals("x", cfg.name.get().value);
    }

    @Test
    void defaultValue_isKeptWhenKeyMissing() {
        DefaultValue cfg = ok("return {}", DefaultValue.class);
        assertEquals("default", cfg.name.value);
    }

    @Test
    void extraKeys_areIgnored() {
        ExtraKeysIgnored cfg = ok("return { name = 'ok', extra = 123, other = { a = 1 } }", ExtraKeysIgnored.class);
        assertEquals("ok", cfg.name.value);
    }

    @Test
    void defaultNestedObject_isKeptWhenKeyMissing() {
        DefaultNestedObjectKept cfg = ok("return {}", DefaultNestedObjectKept.class);
        assertNotNull(cfg.db);
        assertEquals("localhost", cfg.db.host.value);
        assertEquals(Optional.empty(), cfg.db.user);
    }
}
