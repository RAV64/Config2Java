package org.msuo.config2java;

import java.util.ArrayList;
import java.util.List;

final class ErrorCollector {

    private final List<ErrorItem> errors = new ArrayList<>();

    void add(Path path, ConfigErrorType errorType) {
        errors.add(new ErrorItem(path, errorType));
    }

    boolean hasErrors() {
        return !errors.isEmpty();
    }

    List<ConfigDeserializationException.ConfigError> asList() {
        List<ConfigDeserializationException.ConfigError> out = new ArrayList<>(errors.size());
        for (int i = 0; i < errors.size(); i++) {
            ErrorItem item = errors.get(i);
            out.add(
                new ConfigDeserializationException.ConfigError(
                    toPathSegments(item.path),
                    item.errorType
                )
            );
        }
        return out;
    }

    private static final class ErrorItem {
        private final Path path;
        private final ConfigErrorType errorType;

        private ErrorItem(Path path, ConfigErrorType errorType) {
            this.path = path;
            this.errorType = errorType;
        }
    }

    private static List<String> toPathSegments(Path path) {
        Path.Segment[] segments = path.segments();
        List<String> out = new ArrayList<>(Math.max(0, segments.length - 1));
        for (int i = 1; i < segments.length; i++) {
            out.add(PathTextFormatter.label(segments[i]));
        }
        return out;
    }
}
