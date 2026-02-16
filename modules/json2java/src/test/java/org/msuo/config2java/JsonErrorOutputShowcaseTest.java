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

        assertHasError(ex, ConfigErrorTypes.CtorRejected.class, "db", "host");
        assertHasError(ex, ConfigErrorTypes.CtorRejected.class, "db", "port");
        assertHasError(ex, ConfigErrorTypes.EnumUnknown.class, "service", "mode");
        assertHasError(ex, ConfigErrorTypes.CtorRejected.class, "service", "auth", "token");
        assertHasError(ex, ConfigErrorTypes.CtorRejected.class, "service", "auth", "ttl");
        assertHasError(ex, ConfigErrorTypes.CtorRejected.class, "limits", "[bad]");
        assertHasError(ex, ConfigErrorTypes.NoNoArgCtor.class, "bad");
        assertHasError(ex, ConfigErrorTypes.MissingRequiredField.class, "feature", "name");
        assertHasError(ex, ConfigErrorTypes.NoOneArgCtor.class, "ratio");

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
        Class<? extends ConfigErrorType> type,
        String... segments
    ) {
        java.util.List<String> expected = java.util.Arrays.asList(segments);
        for (ConfigDeserializationException.ConfigError e : ex.getErrors()) {
            if (
                e.getErrorType().getClass() == type &&
                e.getPathSegments().equals(expected)
            ) {
                return;
            }
        }
        fail("Missing error type/path: " + type.getSimpleName() + " " + expected);
    }
}
