package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CoreNestedGenericsTest extends CoreMapperContractSupport {

    @Test
    void nestedGenericObjectGraph_success() {
        NestedGenericObjectGraph data = ok(
            obj("foo", obj("value", arr(obj("payload", "a"), obj("payload", "b")))),
            NestedGenericObjectGraph.class
        );

        assertEquals("a", data.foo.value.get(0).payload);
        assertEquals("b", data.foo.value.get(1).payload);
    }

    @Test
    void nestedGenericKeyedListMap_success() {
        NestedGenericKeyedListMap data = ok(
            obj("values", obj("k1", arr("x", "y"), "k2", arr("z"))),
            NestedGenericKeyedListMap.class
        );

        assertEquals(2, data.values.size());
        assertEquals(
            2,
            data.values.get(new StringConstructedGenericKey<Integer>("k1")).size()
        );
    }

    @Test
    void nestedGenericObjectGraph_deepInvalidValue_reportsNoOneArgCtor() {
        DataDeserializationException ex = fails(
            obj("foo", obj("value", arr(obj("payload", 1)))),
            NestedGenericObjectGraph.class
        );
        assertSingleError(
            ex,
            DataErrorTypes.NoOneArgCtor.class,
            "foo",
            "value",
            "[1]",
            "payload"
        );
    }

    @Test
    void nestedGenericKeyedListMap_deepInvalidValue_reportsNoOneArgCtor() {
        DataDeserializationException ex = fails(
            obj("values", obj("k1", arr(1))),
            NestedGenericKeyedListMap.class
        );
        assertSingleError(
            ex,
            DataErrorTypes.NoOneArgCtor.class,
            "values",
            "[k1]",
            "[1]"
        );
    }
}
