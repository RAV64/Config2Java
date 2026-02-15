package org.msuo.config2java;

import java.util.ArrayList;
import java.util.List;

final class ErrorCollector {

    private final List<ConfigDeserializationException.ConfigError> errors =
        new ArrayList<>();

    void add(Path path, ConfigErrorType errorType) {
        errors.add(
            new ConfigDeserializationException.ConfigError(path.toString(), errorType)
        );
    }

    boolean hasErrors() {
        return !errors.isEmpty();
    }

    List<ConfigDeserializationException.ConfigError> asList() {
        return errors;
    }
}
