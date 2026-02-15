package org.msuo.config2java;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class EnvironmentValues {

    private EnvironmentValues() {}

    static Map<String, String> withSystemFallback(Map<String, String> overrides) {
        return new AbstractMap<String, String>() {
            @Override
            public String get(Object key) {
                if (!(key instanceof String)) return null;
                String k = (String) key;
                if (overrides.containsKey(k)) return overrides.get(k);
                return System.getenv(k);
            }

            @Override
            public boolean containsKey(Object key) {
                if (!(key instanceof String)) return false;
                String k = (String) key;
                return overrides.containsKey(k) || System.getenv(k) != null;
            }

            @Override
            public Set<Entry<String, String>> entrySet() {
                LinkedHashMap<String, String> merged = new LinkedHashMap<>(System.getenv());
                merged.putAll(overrides);
                return merged.entrySet();
            }
        };
    }
}
