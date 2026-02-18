package org.msuo.data2java;

import java.util.Collections;
import java.util.Map;

public interface ScriptDeserializer extends Deserializer {

    <T> T deserialize(
        String source,
        Class<T> targetClass,
        Map<String, String> environment,
        Map<String, ?> globals
    );

    @Override
    default <T> T deserialize(String source, Class<T> targetClass) {
        return deserialize(
            source,
            targetClass,
            Collections.emptyMap(),
            Collections.emptyMap()
        );
    }
}
