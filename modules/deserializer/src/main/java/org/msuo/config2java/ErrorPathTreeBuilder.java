package org.msuo.config2java;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ErrorPathTreeBuilder {

    private ErrorPathTreeBuilder() {}

    static ConfigDeserializationException.PathNode build(
        List<ConfigDeserializationException.ConfigError> errors
    ) {
        MutablePathNode root = new MutablePathNode("$");
        for (int i = 0; i < errors.size(); i++) {
            ConfigDeserializationException.ConfigError error = errors.get(i);
            MutablePathNode current = root;
            List<String> segments = error.getPathSegments();
            for (int j = 0; j < segments.size(); j++) {
                String label = segments.get(j);
                current = current.children.computeIfAbsent(
                    label,
                    MutablePathNode::new
                );
            }
            current.errors.add(error);
        }
        return toImmutable(root);
    }

    private static ConfigDeserializationException.PathNode toImmutable(
        MutablePathNode node
    ) {
        List<ConfigDeserializationException.PathNode> children = new ArrayList<>(
            node.children.size()
        );
        for (MutablePathNode child : node.children.values()) {
            children.add(toImmutable(child));
        }
        return new ConfigDeserializationException.PathNode(
            node.segment,
            children,
            node.errors
        );
    }

    private static final class MutablePathNode {
        private final String segment;
        private final Map<String, MutablePathNode> children = new LinkedHashMap<>();
        private final List<ConfigDeserializationException.ConfigError> errors =
            new ArrayList<>();

        private MutablePathNode(String segment) {
            this.segment = segment;
        }
    }
}
