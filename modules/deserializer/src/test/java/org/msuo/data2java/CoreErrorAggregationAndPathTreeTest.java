package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class CoreErrorAggregationAndPathTreeTest extends CoreMapperContractSupport {

    static final class CollectAll {
        public NonEmptyString a;
        public PositiveInteger b;
    }

    @Test
    void aggregatesMultipleErrors() {
        DataDeserializationException ex = fails(
            obj("a", "", "b", 0),
            CollectAll.class
        );
        assertEquals(2, ex.getErrors().size());
    }

    @Test
    void pathTreeContainsRootChildren() {
        DataDeserializationException ex = fails(
            obj("a", "", "b", 0),
            CollectAll.class
        );

        DataDeserializationException.PathNode root = ex.getErrorPathTree();
        assertEquals("$", root.getSegment());

        Set<String> children = new HashSet<>();
        for (DataDeserializationException.PathNode child : root.getChildren()) {
            children.add(child.getSegment());
        }
        assertTrue(children.contains("a"));
        assertTrue(children.contains("b"));
    }

    @Test
    void exceptionMessageRendersTreeOutput() {
        DataDeserializationException ex = fails(
            obj("a", "", "b", 0),
            CollectAll.class
        );
        String message = ex.getMessage();
        assertTrue(message.contains("Data deserialization failed:"));
        assertTrue(message.contains("a"));
        assertTrue(message.contains("b"));
    }
}
