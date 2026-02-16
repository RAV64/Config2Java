package org.msuo.config2java;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractScriptDeserializer implements Deserializer {

    protected static Map<String, String> normalizeEnvironment(
        Map<String, String> environment
    ) {
        return environment == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(environment));
    }

    protected static Map<String, Object> normalizeGlobals(Map<String, ?> globals) {
        return globals == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(globals));
    }
}
