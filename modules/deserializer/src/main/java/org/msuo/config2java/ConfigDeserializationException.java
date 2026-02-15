package org.msuo.config2java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConfigDeserializationException extends RuntimeException {

    private final List<ConfigError> errors;

    public ConfigDeserializationException(List<ConfigError> errors) {
        super(buildMessage(errors));
        this.errors = Collections.unmodifiableList(new ArrayList<ConfigError>(errors));
    }

    public List<ConfigError> getErrors() {
        return errors;
    }

    private static String buildMessage(List<ConfigError> errors) {
        StringBuilder sb = new StringBuilder("Config deserialization failed:\n");
        for (ConfigError e : errors) {
            sb
                .append(" - ")
                .append(e.getPath())
                .append(": ")
                .append(e.getMessage())
                .append("\n");
        }
        return sb.toString();
    }

    public static final class ConfigError {

        private final String path;
        private final ConfigErrorType errorType;

        public ConfigError(String path, ConfigErrorType errorType) {
            this.path = path;
            this.errorType = errorType;
        }

        public String getPath() {
            return path;
        }

        public String getMessage() {
            return errorType.message();
        }

        public ConfigErrorType getErrorType() {
            return errorType;
        }
    }
}
