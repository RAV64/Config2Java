package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CoreNestedGenericsTest extends CoreMapperContractSupport {

    @Test
    void nestedGenericObjectGraph_success() {
        NestedGenericObjectGraph cfg = ok(
            obj("foo", obj("value", arr(obj("payload", "a"), obj("payload", "b")))),
            NestedGenericObjectGraph.class
        );

        assertEquals("a", cfg.foo.value.get(0).payload);
        assertEquals("b", cfg.foo.value.get(1).payload);
    }

    @Test
    void nestedGenericKeyedListMap_success() {
        NestedGenericKeyedListMap cfg = ok(
            obj("values", obj("k1", arr("x", "y"), "k2", arr("z"))),
            NestedGenericKeyedListMap.class
        );

        assertEquals(2, cfg.values.size());
        assertEquals(
            2,
            cfg.values.get(new StringConstructedGenericKey<Integer>("k1")).size()
        );
    }

    @Test
    void nestedGenericObjectGraph_deepInvalidValue_reportsNoOneArgCtor() {
        ConfigDeserializationException ex = fails(
            obj("foo", obj("value", arr(obj("payload", 1)))),
            NestedGenericObjectGraph.class
        );
        assertSingleError(
            ex,
            ConfigErrorTypes.NoOneArgCtor.class,
            "foo",
            "value",
            "[1]",
            "payload"
        );
    }

    @Test
    void nestedGenericKeyedListMap_deepInvalidValue_reportsNoOneArgCtor() {
        ConfigDeserializationException ex = fails(
            obj("values", obj("k1", arr(1))),
            NestedGenericKeyedListMap.class
        );
        assertSingleError(
            ex,
            ConfigErrorTypes.NoOneArgCtor.class,
            "values",
            "[k1]",
            "[1]"
        );
    }
}
