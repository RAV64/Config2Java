package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
public class JsonMissingAndDefaultsTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new JsonDeserializer();
    }


    @Test
    void missingRequired_withoutDefault_fails() {
        ConfigDeserializationException ex = fails("{}", CfgMissingRequired.class);
        assertSingleError(ex, "$.name", "Missing required field");
    }

    @Test
    void missingOptional_defaultsToEmpty() {
        CfgMissingOptional cfg = ok("{}", CfgMissingOptional.class);
        assertEquals(Optional.empty(), cfg.name);
    }

    @Test
    void optionalDefaultPresent_isKeptWhenKeyMissing() {
        CfgOptionalHasDefaultPresent cfg = ok("{}", CfgOptionalHasDefaultPresent.class);
        assertTrue(cfg.name.isPresent());
        assertEquals("x", cfg.name.get().value);
    }

    @Test
    void defaultValue_isKeptWhenKeyMissing() {
        CfgDefaultValue cfg = ok("{}", CfgDefaultValue.class);
        assertEquals("default", cfg.name.value);
    }

    @Test
    void extraKeys_areIgnored() {
        CfgExtraKeysIgnored cfg = ok("{\"name\":\"ok\",\"extra\":123,\"other\":{\"a\":1}}", CfgExtraKeysIgnored.class);
        assertEquals("ok", cfg.name.value);
    }

    @Test
    void defaultNestedObject_isKeptWhenKeyMissing() {
        CfgDefaultNestedObjectKept cfg = ok("{}", CfgDefaultNestedObjectKept.class);
        assertNotNull(cfg.db);
        assertEquals("localhost", cfg.db.host.value);
        assertEquals(Optional.empty(), cfg.db.user);
    }
}
