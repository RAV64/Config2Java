package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class LuaEnvironmentAndGlobalsTest extends LuaContractSupport {
    private static final String ENV_KEY = "CONFIG2JAVA_TEST_APP_ENV_6A0C9341_EE7F_4F5A_BA4A";
    private final ScriptDeserializer deserializer = new LuaDeserializer();

    @Test
    void envAbsent_usesDefaultBranch() {
        Map<String, String> environment = new LinkedHashMap<>();
        environment.put(ENV_KEY, null);
        Map<String, Object> globals = Map.of("defaultName", "worker-default");

        Injected cfg = deserializer.deserialize(
            "local cfg = { mode = 'DEV', name = defaultName }\n" +
            "if os.getenv('" + ENV_KEY + "') == 'prod' then\n" +
            "  cfg.mode = 'PROD'\n" +
            "end\n" +
            "return cfg",
            Injected.class,
            environment,
            globals
        );

        assertEquals(Mode.DEV, cfg.mode);
        assertEquals("worker-default", cfg.name.value);
    }

    @Test
    void envPresent_switchesBranch() {
        Map<String, String> environment = Map.of(ENV_KEY, "prod");
        Map<String, Object> globals = Map.of("defaultName", "worker-default");

        Injected cfg = deserializer.deserialize(
            "local cfg = { mode = 'DEV', name = defaultName }\n" +
            "if os.getenv('" + ENV_KEY + "') == 'prod' then\n" +
            "  cfg.mode = 'PROD'\n" +
            "end\n" +
            "return cfg",
            Injected.class,
            environment,
            globals
        );

        assertEquals(Mode.PROD, cfg.mode);
        assertEquals("worker-default", cfg.name.value);
    }

    @Test
    void canImportLuaFile_modifyImportedContent_andReturnForBinding()
        throws URISyntaxException {
        String luaPath = luaString(resourcePath("/corpus/import/base-config.lua").toString());
        String mainScript =
            "local cfg = dofile(" + luaPath + ")\n" +
            "cfg.mode = 'PROD'\n" +
            "cfg.name = cfg.name .. '-updated'\n" +
            "return cfg";

        Injected cfg = deserializer.deserialize(mainScript, Injected.class);

        assertEquals(Mode.PROD, cfg.mode);
        assertEquals("from-file-updated", cfg.name.value);
    }

    private static String luaString(String raw) {
        return "\"" + raw.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static Path resourcePath(String resource) throws URISyntaxException {
        java.net.URL url = LuaEnvironmentAndGlobalsTest.class.getResource(resource);
        assertNotNull(url, "Missing test resource: " + resource);
        return Paths.get(url.toURI());
    }
}
