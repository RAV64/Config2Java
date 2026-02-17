package org.msuo.config2java;

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
        ConfigDeserializationException ex = fails(
            obj("a", "", "b", 0),
            CollectAll.class
        );
        assertEquals(2, ex.getErrors().size());
    }

    @Test
    void pathTreeContainsRootChildren() {
        ConfigDeserializationException ex = fails(
            obj("a", "", "b", 0),
            CollectAll.class
        );

        ConfigDeserializationException.PathNode root = ex.getErrorPathTree();
        assertEquals("$", root.getSegment());

        Set<String> children = new HashSet<>();
        for (ConfigDeserializationException.PathNode child : root.getChildren()) {
            children.add(child.getSegment());
        }
        assertTrue(children.contains("a"));
        assertTrue(children.contains("b"));
    }

    @Test
    void exceptionMessageRendersTreeOutput() {
        ConfigDeserializationException ex = fails(
            obj("a", "", "b", 0),
            CollectAll.class
        );
        String message = ex.getMessage();
        assertTrue(message.contains("Config deserialization failed:"));
        assertTrue(message.contains("a"));
        assertTrue(message.contains("b"));
    }
}
