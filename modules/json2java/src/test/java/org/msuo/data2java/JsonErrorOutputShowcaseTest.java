package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

public class JsonErrorOutputShowcaseTest extends JsonContractSupport {

    @Test
    void showcase_how_aggregated_error_output_looks() {
        String source =
            "{\"db\":{\"host\":\"\",\"port\":0},"
                + "\"service\":{\"mode\":\"NOPE\",\"auth\":{\"token\":\"\",\"ttl\":0}},"
                + "\"limits\":{\"ok\":1,\"bad\":0},"
                + "\"bad\":{\"x\":\"ok\"},"
                + "\"feature\":{},"
                + "\"ratio\":1}";

        DataDeserializationException ex = fails(source, ShowcaseCfg.class);

        // Stable assertions via API (order-independent).
        assertEquals(9, ex.getErrors().size());
        assertErrorTreeRootChildren(ex, "db", "service", "limits", "bad", "feature", "ratio");

        assertHasError(ex, DataErrorTypes.CtorRejected.class, "db", "host");
        assertHasError(ex, DataErrorTypes.CtorRejected.class, "db", "port");
        assertHasError(ex, DataErrorTypes.EnumUnknown.class, "service", "mode");
        assertHasError(ex, DataErrorTypes.CtorRejected.class, "service", "auth", "token");
        assertHasError(ex, DataErrorTypes.CtorRejected.class, "service", "auth", "ttl");
        assertHasError(ex, DataErrorTypes.CtorRejected.class, "limits", "[bad]");
        assertHasError(ex, DataErrorTypes.NoNoArgCtor.class, "bad");
        assertHasError(ex, DataErrorTypes.MissingRequiredField.class, "feature", "name");
        assertHasError(ex, DataErrorTypes.NoOneArgCtor.class, "ratio");

        assertTrue(ex.getMessage().startsWith("Data deserialization failed:\n$"));
    }

    @Test
    void showcase_stacktrace_contains_exception_tree_and_frames() {
        String source = "{\"db\":{\"host\":\"\",\"port\":0}}";

        DataDeserializationException ex = fails(source, ShowcaseDbOnlyCfg.class);

        assertTrue(
            ex.getMessage().startsWith("Data deserialization failed:\n$"),
            "Expected aggregated tree message prefix"
        );
        assertEquals(2, ex.getErrors().size());
        assertErrorType(ex, 0, DataErrorTypes.CtorRejected.class);
        assertErrorType(ex, 1, DataErrorTypes.CtorRejected.class);

        StackTraceElement top = ex.getStackTrace()[0];
        assertEquals("org.msuo.data2java.ObjectMapper", top.getClassName());
        assertEquals("deserialize", top.getMethodName());

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();
        assertTrue(trace.contains("org.msuo.data2java.DataDeserializationException"));
        assertTrue(trace.contains("Data deserialization failed:"));
        assertTrue(trace.contains("org.msuo.data2java.ObjectMapper.deserialize"));
        assertTrue(trace.contains("org.msuo.data2java.JsonDeserializer.deserialize"));
    }

    static final class ShowcaseCfg {
        public Db db;
        public Service service;
        public java.util.Map<NonEmptyString, PositiveInteger> limits;
        public NoNoArgNested bad;
        public Feature feature;
        public PositiveDouble ratio;
    }

    static final class ShowcaseDbOnlyCfg {
        public Db db;
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
        DataDeserializationException ex,
        Class<? extends DataErrorType> type,
        String... segments
    ) {
        java.util.List<String> expected = java.util.Arrays.asList(segments);
        for (DataDeserializationException.DataError e : ex.getErrors()) {
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
