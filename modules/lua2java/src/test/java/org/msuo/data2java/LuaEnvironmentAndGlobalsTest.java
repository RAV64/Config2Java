package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class LuaEnvironmentAndGlobalsTest extends LuaContractSupport {
    private static final String ENV_KEY = "DATA2JAVA_TEST_APP_ENV_6A0C9341_EE7F_4F5A_BA4A";
    private final ScriptDeserializer deserializer = new LuaDeserializer();

    @Test
    void envAbsent_usesDefaultBranch() {
        Map<String, String> environment = new LinkedHashMap<>();
        environment.put(ENV_KEY, null);
        Map<String, Object> globals = Map.of("defaultName", "worker-default");

        Injected data = deserializer.deserialize(
            "local data = { mode = 'DEV', name = defaultName }\n" +
            "if os.getenv('" + ENV_KEY + "') == 'prod' then\n" +
            "  data.mode = 'PROD'\n" +
            "end\n" +
            "return data",
            Injected.class,
            environment,
            globals
        );

        assertEquals(Mode.DEV, data.mode);
        assertEquals("worker-default", data.name.value);
    }

    @Test
    void envPresent_switchesBranch() {
        Map<String, String> environment = Map.of(ENV_KEY, "prod");
        Map<String, Object> globals = Map.of("defaultName", "worker-default");

        Injected data = deserializer.deserialize(
            "local data = { mode = 'DEV', name = defaultName }\n" +
            "if os.getenv('" + ENV_KEY + "') == 'prod' then\n" +
            "  data.mode = 'PROD'\n" +
            "end\n" +
            "return data",
            Injected.class,
            environment,
            globals
        );

        assertEquals(Mode.PROD, data.mode);
        assertEquals("worker-default", data.name.value);
    }

    @Test
    void canLoadLuaFile_modifyLoadedContent_andReturnForBinding()
        throws URISyntaxException {
        String luaPath = luaString(resourcePath("/corpus/load/base-data.lua").toString());
        String mainScript =
            "local data = dofile(" + luaPath + ")\n" +
            "data.mode = 'PROD'\n" +
            "data.name = data.name .. '-updated'\n" +
            "return data";

        Injected data = deserializer.deserialize(mainScript, Injected.class);

        assertEquals(Mode.PROD, data.mode);
        assertEquals("from-file-updated", data.name.value);
    }

    @Test
    void nullEnvironmentKey_isRejectedEarly() {
        Map<String, String> environment = new LinkedHashMap<>();
        environment.put(null, "x");

        assertThrows(
            IllegalArgumentException.class,
            () -> deserializer.deserialize(
                "return { mode = 'DEV', name = 'x' }",
                Injected.class,
                environment,
                Map.of()
            )
        );
    }

    @Test
    void blankEnvironmentKey_isRejectedEarly() {
        Map<String, String> environment = new LinkedHashMap<>();
        environment.put("   ", "x");

        assertThrows(
            IllegalArgumentException.class,
            () -> deserializer.deserialize(
                "return { mode = 'DEV', name = 'x' }",
                Injected.class,
                environment,
                Map.of()
            )
        );
    }

    @Test
    void nullGlobalKey_isRejectedEarly() {
        Map<String, Object> globals = new LinkedHashMap<>();
        globals.put(null, "x");

        assertThrows(
            IllegalArgumentException.class,
            () -> deserializer.deserialize(
                "return { mode = 'DEV', name = 'x' }",
                Injected.class,
                Map.of(),
                globals
            )
        );
    }

    @Test
    void blankGlobalKey_isRejectedEarly() {
        Map<String, Object> globals = new LinkedHashMap<>();
        globals.put("  ", "x");

        assertThrows(
            IllegalArgumentException.class,
            () -> deserializer.deserialize(
                "return { mode = 'DEV', name = 'x' }",
                Injected.class,
                Map.of(),
                globals
            )
        );
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
