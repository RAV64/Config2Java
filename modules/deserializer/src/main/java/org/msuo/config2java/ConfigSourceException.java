package org.msuo.config2java;

public final class ConfigSourceException extends RuntimeException {

    private final String format;
    private final String phase;

    public ConfigSourceException(
        String format,
        String phase,
        String message,
        Throwable cause
    ) {
        super("Failed to " + phase + " " + format + ": " + message, cause);
        this.format = format;
        this.phase = phase;
    }

    public String format() {
        return format;
    }

    public String phase() {
        return phase;
    }
}
