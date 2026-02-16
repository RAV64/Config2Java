package org.msuo.config2java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class ConfigDeserializationException extends RuntimeException {

    private final List<ConfigError> errors;

    public ConfigDeserializationException(List<ConfigError> errors) {
        super("Config deserialization failed");
        this.errors = Collections.unmodifiableList(new ArrayList<ConfigError>(errors));
    }

    public List<ConfigError> getErrors() {
        return errors;
    }

    public void forEachError(BiConsumer<List<String>, ConfigError> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        for (int i = 0; i < errors.size(); i++) {
            ConfigError error = errors.get(i);
            consumer.accept(error.getPathSegments(), error);
        }
    }

    public PathNode getErrorPathTree() {
        return ErrorPathTreeBuilder.build(errors);
    }

    @Override
    public String getMessage() {
        return "Config deserialization failed:\n" + PathTreeFormatter.format(getErrorPathTree());
    }

    public static final class PathNode {
        private final String segment;
        private final List<PathNode> children;
        private final List<ConfigError> errors;

        PathNode(
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

        ConfigError(List<String> pathSegments, ConfigErrorType errorType) {
            this.pathSegments = Collections.unmodifiableList(
                new ArrayList<String>(pathSegments)
            );
            this.errorType = errorType;
        }

        public List<String> getPathSegments() {
            return pathSegments;
        }

        public String getMessage() {
            return errorType.message();
        }

        public ConfigErrorType getErrorType() {
            return errorType;
        }
    }
}
