package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class GroovyEnvironmentAndGlobalsTest extends GroovyContractSupport {
    private static final String ENV_KEY = "DATA2JAVA_TEST_APP_ENV_6A0C9341_EE7F_4F5A_BA4A";
    private final ScriptDeserializer deserializer = new GroovyDeserializer();

    @Test
    void envAbsent_usesDefaultBranch() {
        Map<String, String> environment = new LinkedHashMap<>();
        environment.put(ENV_KEY, null);
        Map<String, Object> globals = Map.of("defaultName", "worker-default");

        Injected data = deserializer.deserialize(
            "def data = [mode: 'DEV', name: defaultName]\n" +
            "if (ENV." + ENV_KEY + " == 'prod') {\n" +
            "  data.mode = 'PROD'\n" +
            "}\n" +
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
            "def data = [mode: 'DEV', name: defaultName]\n" +
            "if (ENV." + ENV_KEY + " == 'prod') {\n" +
            "  data.mode = 'PROD'\n" +
            "}\n" +
            "return data",
            Injected.class,
            environment,
            globals
        );

        assertEquals(Mode.PROD, data.mode);
        assertEquals("worker-default", data.name.value);
    }

    @Test
    void scriptWithoutReturn_usesImplicitGroovyReturn() {
        Injected data = deserializer.deserialize(
            "[mode: 'DEV', name: 'n']",
            Injected.class
        );

        assertEquals(Mode.DEV, data.mode);
        assertEquals("n", data.name.value);
    }

    @Test
    void canLoadGroovyFile_modifyLoadedContent_andReturnForBinding()
        throws URISyntaxException {
        String path = groovyString(resourcePath("/corpus/load/base-data.groovy").toString());
        String mainScript =
            "def data = evaluate(new File(" + path + "))\n" +
            "data.mode = 'PROD'\n" +
            "data.name = data.name + '-updated'\n" +
            "return data";

        Injected data = deserializer.deserialize(mainScript, Injected.class);

        assertEquals(Mode.PROD, data.mode);
        assertEquals("from-file-updated", data.name.value);
    }

    @Test
    void loadFunction_supportsSiblingParentAndChildRelativePaths()
        throws Exception {
        Path main = resourcePath("/corpus/load/tree/nested/main.groovy");

        Injected data = deserializer.deserialize(main, Injected.class);

        assertEquals(Mode.DEV, data.mode);
        assertEquals("foo-bar-baz", data.name.value);
    }

    @Test
    void loadFunction_supportsAbsolutePaths() throws Exception {
        Path imported = resourcePath("/corpus/load/tree/bar.groovy").toAbsolutePath();
        String script =
            "def x = load('" + groovyPathLiteral(imported.toString()) + "')\n" +
            "return [mode: 'PROD', name: x.name]";

        Injected data = deserializer.deserialize(script, Injected.class);

        assertEquals(Mode.PROD, data.mode);
        assertEquals("bar", data.name.value);
    }

    @Test
    void loadFunction_loadedFile_canReadInjectedEnvAndGlobals()
        throws Exception {
        Path loaded = resourcePath("/corpus/load/env/loaded-env-and-globals.groovy")
            .toAbsolutePath();
        String script =
            "return load('" + groovyPathLiteral(loaded.toString()) + "')";

        Injected data = deserializer.deserialize(
            script,
            Injected.class,
            Map.of(ENV_KEY, "prod"),
            Map.of("defaultName", "from-global")
        );

        assertEquals(Mode.PROD, data.mode);
        assertEquals("from-global", data.name.value);
    }

    @Test
    void loadFunction_nestedRelativeLoadedFile_canReadInjectedEnvAndGlobals()
        throws Exception {
        Path main = resourcePath("/corpus/load/env/nested/main.groovy")
            .toAbsolutePath();
        String script =
            "return load('" + groovyPathLiteral(main.toString()) + "')";

        Injected data = deserializer.deserialize(
            script,
            Injected.class,
            Map.of(ENV_KEY, "prod"),
            Map.of("defaultName", "from-global")
        );

        assertEquals(Mode.PROD, data.mode);
        assertEquals("from-global-nested", data.name.value);
    }

    @Test
    void nullEnvironmentKey_isRejectedEarly() {
        Map<String, String> environment = new LinkedHashMap<>();
        environment.put(null, "x");

        assertThrows(
            IllegalArgumentException.class,
            () -> deserializer.deserialize(
                "return [mode: 'DEV', name: 'x']",
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
                "return [mode: 'DEV', name: 'x']",
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
                "return [mode: 'DEV', name: 'x']",
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
                "return [mode: 'DEV', name: 'x']",
                Injected.class,
                Map.of(),
                globals
            )
        );
    }

    private static String groovyString(String raw) {
        return "'" + raw.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private static String groovyPathLiteral(String raw) {
        return raw.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static Path resourcePath(String resource) throws URISyntaxException {
        java.net.URL url = GroovyEnvironmentAndGlobalsTest.class.getResource(resource);
        assertNotNull(url, "Missing test resource: " + resource);
        return Paths.get(url.toURI());
    }
}
