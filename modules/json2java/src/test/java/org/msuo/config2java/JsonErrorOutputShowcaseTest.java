package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class JsonErrorOutputShowcaseTest extends SharedContractSupport {

    @Override
    protected Deserializer deserializer() {
        return new JsonDeserializer();
    }

    @Test
    void showcase_how_aggregated_error_output_looks() {
        String source =
            "{\"db\":{\"host\":\"\",\"port\":0},"
                + "\"service\":{\"mode\":\"NOPE\",\"auth\":{\"token\":\"\",\"ttl\":0}},"
                + "\"limits\":{\"ok\":1,\"bad\":0},"
                + "\"bad\":{\"x\":\"ok\"},"
                + "\"feature\":{},"
                + "\"ratio\":1}";

        ConfigDeserializationException ex = fails(source, ShowcaseCfg.class);

        // Stable assertions via API (order-independent).
        assertEquals(9, ex.getErrors().size());
        assertErrorTreeRootChildren(ex, "db", "service", "limits", "bad", "feature", "ratio");

        assertHasError(ex, ConfigErrorKind.CtorRejected, "db", "host");
        assertHasError(ex, ConfigErrorKind.CtorRejected, "db", "port");
        assertHasError(ex, ConfigErrorKind.EnumUnknown, "service", "mode");
        assertHasError(ex, ConfigErrorKind.CtorRejected, "service", "auth", "token");
        assertHasError(ex, ConfigErrorKind.CtorRejected, "service", "auth", "ttl");
        assertHasError(ex, ConfigErrorKind.CtorRejected, "limits", "[bad]");
        assertHasError(ex, ConfigErrorKind.NoNoArgCtor, "bad");
        assertHasError(ex, ConfigErrorKind.MissingRequiredField, "feature", "name");
        assertHasError(ex, ConfigErrorKind.NoOneArgCtor, "ratio");

        // Showcase output for humans when running tests.
        System.out.println("=== ConfigDeserializationException#getMessage() ===");
        System.out.println(ex.getMessage());
    }

    static final class ShowcaseCfg {
        public Db db;
        public Service service;
        public java.util.Map<NonEmptyString, PositiveInteger> limits;
        public NoNoArgNested bad;
        public Feature feature;
        public PositiveDouble ratio;
    }

    static final class Db {
        public NonEmptyString host;
        public PositiveInteger port;
    }

    static final class Service {
        public Mode mode;
        public Auth auth;
    }

    enum Mode {
        DEV,
        PROD,
    }

    static final class Auth {
        public NonEmptyString token;
        public PositiveInteger ttl;
    }

    static final class Feature {
        public NonEmptyString name;
    }

    private static void assertHasError(
        ConfigDeserializationException ex,
        ConfigErrorKind kind,
        String... segments
    ) {
        java.util.List<String> expected = java.util.Arrays.asList(segments);
        for (ConfigDeserializationException.ConfigError e : ex.getErrors()) {
            if (e.getErrorKind() == kind && e.getPathSegments().equals(expected)) {
                return;
            }
        }
        fail("Missing error kind/path: " + kind + " " + expected);
    }
}
