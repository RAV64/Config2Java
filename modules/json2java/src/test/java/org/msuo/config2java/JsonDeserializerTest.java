package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class JsonDeserializerTest {

    enum Mode {
        DEV,
        PROD,
    }

    static final class AppCfg {

        public String host;
        public Integer port;
        public Mode mode;
        public Optional<String> user;
    }

    static final class RootCfg {

        public AppCfg app;
    }

    @Test
    void parsesNestedObjectsAndOptional() {
        String json =
            "{\"app\":{\"host\":\"localhost\",\"port\":8080,\"mode\":\"PROD\"}}";

        RootCfg cfg = new JsonDeserializer().deserialize(json, RootCfg.class);

        assertEquals("localhost", cfg.app.host);
        assertEquals(Integer.valueOf(8080), cfg.app.port);
        assertEquals(Mode.PROD, cfg.app.mode);
        assertEquals(Optional.empty(), cfg.app.user);
    }
}
