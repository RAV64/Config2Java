package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class LuaEnvironmentAndGlobalsTest extends SharedContractSupport {

    @Override
    protected Deserializer deserializer() {
        return new LuaDeserializer();
    }

    @Test
    void envAbsent_usesDefaultBranch() {
        LuaDeserializer d = LuaDeserializer.builder()
            .global("defaultName", "worker-default")
            .build();

        CfgInjected cfg = d.deserialize(
            "local cfg = { mode = 'DEV', name = defaultName }\n" +
            "if os.getenv('APP_ENV') == 'prod' then\n" +
            "  cfg.mode = 'PROD'\n" +
            "end\n" +
            "return cfg",
            CfgInjected.class
        );

        assertEquals(Mode.DEV, cfg.mode);
        assertEquals("worker-default", cfg.name.value);
    }

    @Test
    void envPresent_switchesBranch() {
        LuaDeserializer d = LuaDeserializer.builder()
            .env("APP_ENV", "prod")
            .global("defaultName", "worker-default")
            .build();

        CfgInjected cfg = d.deserialize(
            "local cfg = { mode = 'DEV', name = defaultName }\n" +
            "if os.getenv('APP_ENV') == 'prod' then\n" +
            "  cfg.mode = 'PROD'\n" +
            "end\n" +
            "return cfg",
            CfgInjected.class
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

        CfgInjected cfg = new LuaDeserializer().deserialize(mainScript, CfgInjected.class);

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
