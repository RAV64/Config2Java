package org.msuo.config2java;

import java.util.Collections;
import java.util.Map;

public interface ScriptDeserializer extends Deserializer {

    <T> T deserialize(
        String source,
        Class<T> configClass,
        Map<String, String> environment,
        Map<String, ?> globals
    );

    @Override
    default <T> T deserialize(String source, Class<T> configClass) {
        return deserialize(
            source,
            configClass,
            Collections.emptyMap(),
            Collections.emptyMap()
        );
    }
}
