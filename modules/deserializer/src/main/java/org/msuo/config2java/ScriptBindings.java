package org.msuo.config2java;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScriptBindings {

    private static final ScriptBindings EMPTY = new ScriptBindings(
        Collections.emptyMap(),
        Collections.emptyMap()
    );

    private final Map<String, String> environment;
    private final Map<String, Object> globals;

    private ScriptBindings(
        Map<String, String> environment,
        Map<String, Object> globals
    ) {
        this.environment = Collections.unmodifiableMap(
            new LinkedHashMap<>(environment)
        );
        this.globals = Collections.unmodifiableMap(new LinkedHashMap<>(globals));
    }

    public static ScriptBindings empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, String> environment() {
        return environment;
    }

    public Map<String, Object> globals() {
        return globals;
    }

    public static final class Builder {

        private final Map<String, String> environment = new LinkedHashMap<>();
        private final Map<String, Object> globals = new LinkedHashMap<>();

        public Builder environment(Map<String, String> values) {
            this.environment.clear();
            this.environment.putAll(values);
            return this;
        }

        public Builder env(String key, String value) {
            this.environment.put(key, value);
            return this;
        }

        public Builder globals(Map<String, ?> values) {
            this.globals.clear();
            this.globals.putAll(values);
            return this;
        }

        public Builder global(String key, Object value) {
            this.globals.put(key, value);
            return this;
        }

        public ScriptBindings build() {
            return new ScriptBindings(environment, globals);
        }
    }
}
