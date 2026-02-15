package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class GroovyEnvironmentAndGlobalsTest extends SharedContractSupport {

    @Override
    protected Deserializer deserializer() {
        return new GroovyDeserializer();
    }

    @Test
    void envAbsent_usesDefaultBranch() {
        GroovyDeserializer d = GroovyDeserializer.builder()
            .global("defaultName", "worker-default")
            .build();

        CfgInjected cfg = d.deserialize(
            "def cfg = [mode: 'DEV', name: defaultName]\n" +
            "if (ENV.APP_ENV == 'prod') {\n" +
            "  cfg.mode = 'PROD'\n" +
            "}\n" +
            "return cfg",
            CfgInjected.class
        );

        assertEquals(Mode.DEV, cfg.mode);
        assertEquals("worker-default", cfg.name.value);
    }

    @Test
    void envPresent_switchesBranch() {
        GroovyDeserializer d = GroovyDeserializer.builder()
            .env("APP_ENV", "prod")
            .global("defaultName", "worker-default")
            .build();

        CfgInjected cfg = d.deserialize(
            "def cfg = [mode: 'DEV', name: defaultName]\n" +
            "if (ENV.APP_ENV == 'prod') {\n" +
            "  cfg.mode = 'PROD'\n" +
            "}\n" +
            "return cfg",
            CfgInjected.class
        );

        assertEquals(Mode.PROD, cfg.mode);
        assertEquals("worker-default", cfg.name.value);
    }

    @Test
    void scriptWithoutReturn_usesImplicitGroovyReturn() {
        CfgInjected cfg = new GroovyDeserializer().deserialize(
            "[mode: 'DEV', name: 'n']",
            CfgInjected.class
        );

        assertEquals(Mode.DEV, cfg.mode);
        assertEquals("n", cfg.name.value);
    }

    @Test
    void canImportGroovyFile_modifyImportedContent_andReturnForBinding()
        throws URISyntaxException {
        String path = groovyString(resourcePath("/corpus/import/base-config.groovy").toString());
        String mainScript =
            "def cfg = evaluate(new File(" + path + "))\n" +
            "cfg.mode = 'PROD'\n" +
            "cfg.name = cfg.name + '-updated'\n" +
            "return cfg";

        CfgInjected cfg = new GroovyDeserializer().deserialize(mainScript, CfgInjected.class);

        assertEquals(Mode.PROD, cfg.mode);
        assertEquals("from-file-updated", cfg.name.value);
    }

    private static String groovyString(String raw) {
        return "'" + raw.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private static Path resourcePath(String resource) throws URISyntaxException {
        java.net.URL url = GroovyEnvironmentAndGlobalsTest.class.getResource(resource);
        assertNotNull(url, "Missing test resource: " + resource);
        return Paths.get(url.toURI());
    }
}
