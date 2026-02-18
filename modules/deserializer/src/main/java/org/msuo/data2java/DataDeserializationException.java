package org.msuo.data2java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class DataDeserializationException extends RuntimeException {

    private final List<DataError> errors;

    public DataDeserializationException(List<DataError> errors) {
        super("Data deserialization failed");
        this.errors = Collections.unmodifiableList(
            new ArrayList<DataError>(requireNonEmpty(errors, "errors"))
        );
    }

    private static <T extends Collection<?>> T requireNonEmpty(T value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return value;
    }

    public List<DataError> getErrors() {
        return errors;
    }

    public void forEachError(BiConsumer<List<String>, DataError> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        for (int i = 0; i < errors.size(); i++) {
            DataError error = errors.get(i);
            consumer.accept(error.getPathSegments(), error);
        }
    }

    public PathNode getErrorPathTree() {
        return ErrorPathTreeBuilder.build(errors);
    }

    @Override
    public String getMessage() {
        return "Data deserialization failed:\n" + PathTreeFormatter.format(getErrorPathTree());
    }

    public static final class PathNode {
        private final String segment;
        private final List<PathNode> children;
        private final List<DataError> errors;

        PathNode(
            String segment,
            List<PathNode> children,
            List<DataError> errors
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

        public List<DataError> getErrors() {
            return errors;
        }
    }

    public static final class DataError {

        private final List<String> pathSegments;
        private final DataErrorType errorType;

        DataError(List<String> pathSegments, DataErrorType errorType) {
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

        public DataErrorType getErrorType() {
            return errorType;
        }
    }
}
