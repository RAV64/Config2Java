package org.msuo.config2java;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractScriptDeserializer implements ScriptDeserializer {

    @Override
    public final <T> T deserialize(
        String source,
        Class<T> configClass,
        Map<String, String> environment,
        Map<String, ?> globals
    ) {
        return ObjectMapper.deserialize(
            parse(
                source,
                normalizeEnvironment(environment),
                normalizeGlobals(globals)
            ),
            configClass
        );
    }

    protected abstract ConfigValue parse(
        String source,
        Map<String, String> environment,
        Map<String, Object> globals
    );

    private static Map<String, String> normalizeEnvironment(
        Map<String, String> environment
    ) {
        if (environment == null) return Collections.emptyMap();
        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            String key = requireValidKey(entry.getKey(), "environment");
            normalized.put(key, entry.getValue());
        }
        return Collections.unmodifiableMap(normalized);
    }

    private static Map<String, Object> normalizeGlobals(Map<String, ?> globals) {
        if (globals == null) return Collections.emptyMap();
        LinkedHashMap<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : globals.entrySet()) {
            String key = requireValidKey(entry.getKey(), "globals");
            normalized.put(key, entry.getValue());
        }
        return Collections.unmodifiableMap(normalized);
    }

    private static String requireValidKey(String key, String sourceName) {
        if (key == null) {
            throw new IllegalArgumentException(
                "Invalid " + sourceName + " key: key must not be null"
            );
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Invalid " + sourceName + " key: key must not be blank"
            );
        }
        return key;
    }
}
