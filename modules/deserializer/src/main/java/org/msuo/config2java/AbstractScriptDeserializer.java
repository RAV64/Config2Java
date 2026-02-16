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
        return environment == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(environment));
    }

    private static Map<String, Object> normalizeGlobals(Map<String, ?> globals) {
        return globals == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(globals));
    }
}
