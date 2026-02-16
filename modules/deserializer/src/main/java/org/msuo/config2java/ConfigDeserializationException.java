package org.msuo.config2java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConfigDeserializationException extends RuntimeException {

    private final List<ConfigError> errors;
    private PathNode cachedPathTreeModel;
    private String cachedMessage;

    public ConfigDeserializationException(List<ConfigError> errors) {
        super("Config deserialization failed");
        this.errors = Collections.unmodifiableList(new ArrayList<ConfigError>(errors));
    }

    public List<ConfigError> getErrors() {
        return errors;
    }

    public PathNode getErrorPathTree() {
        if (cachedPathTreeModel == null) {
            cachedPathTreeModel = buildPathTree(errors);
        }
        return cachedPathTreeModel;
    }

    @Override
    public String getMessage() {
        if (cachedMessage != null) return cachedMessage;
        cachedMessage = buildMessage(errors, PathTreeFormatter.format(getErrorPathTree()));
        return cachedMessage;
    }

    private static String buildMessage(List<ConfigError> errors, String pathTree) {
        StringBuilder sb = new StringBuilder("Config deserialization failed");
        if (pathTree != null && !pathTree.isEmpty()) {
            sb.append(":\nError path tree:\n").append(pathTree);
        } else if (!errors.isEmpty()) {
            sb.append(".");
        }
        return sb.toString();
    }

    private static PathNode buildPathTree(List<ConfigError> errors) {
        MutablePathNode root = new MutablePathNode("$");
        for (int i = 0; i < errors.size(); i++) {
            ConfigError error = errors.get(i);
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

    private static PathNode toImmutable(MutablePathNode node) {
        List<PathNode> children = new ArrayList<>(node.children.size());
        for (MutablePathNode child : node.children.values()) {
            children.add(toImmutable(child));
        }
        return new PathNode(node.segment, children, node.errors);
    }

    private static final class MutablePathNode {
        private final String segment;
        private final Map<String, MutablePathNode> children = new LinkedHashMap<>();
        private final List<ConfigError> errors = new ArrayList<>();

        private MutablePathNode(String segment) {
            this.segment = segment;
        }
    }

    public static final class PathNode {
        private final String segment;
        private final List<PathNode> children;
        private final List<ConfigError> errors;

        private PathNode(
            String segment,
            List<PathNode> children,
            List<ConfigError> errors
        ) {
            this.segment = segment;
            this.children = Collections.unmodifiableList(children);
            this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        }

        public String getSegment() {
            return segment;
        }

        public List<PathNode> getChildren() {
            return children;
        }

        public List<ConfigError> getErrors() {
            return errors;
        }
    }

    public static final class ConfigError {

        private final List<String> pathSegments;
        private final ConfigErrorType errorType;

        ConfigError(ConfigErrorType errorType) {
            this(Collections.emptyList(), errorType);
        }

        ConfigError(List<String> pathSegments, ConfigErrorType errorType) {
            this.pathSegments = Collections.unmodifiableList(
                new ArrayList<String>(pathSegments)
            );
            this.errorType = errorType;
        }

        public List<String> getPathSegments() {
            return pathSegments;
        }

        public ConfigErrorKind getErrorKind() {
            return errorType.kind();
        }

        public String getMessage() {
            return errorType.message();
        }
    }
}
