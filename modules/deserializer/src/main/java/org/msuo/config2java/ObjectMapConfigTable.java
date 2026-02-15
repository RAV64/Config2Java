package org.msuo.config2java;

import java.util.Iterator;
import java.util.Map;

final class ObjectMapConfigTable implements ConfigTable {

    interface ValueFactory {
        ConfigValue wrap(Object value);
    }

    private final Map<?, ?> map;
    private final ValueFactory valueFactory;

    ObjectMapConfigTable(Map<?, ?> map, ValueFactory valueFactory) {
        this.map = map;
        this.valueFactory = valueFactory;
    }

    @Override
    public ConfigValue getField(String key) {
        if (!map.containsKey(key)) return MissingValue.INSTANCE;
        return valueFactory.wrap(map.get(key));
    }

    @Override
    public Iterable<ConfigEntry> entries() {
        return () -> new Iterator<ConfigEntry>() {
            private final Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public ConfigEntry next() {
                Map.Entry<?, ?> e = it.next();
                ConfigValue key = e.getKey() == null
                    ? MissingValue.INSTANCE
                    : valueFactory.wrap(e.getKey());
                return ConfigEntry.of(
                    key,
                    valueFactory.wrap(e.getValue()),
                    String.valueOf(e.getKey())
                );
            }
        };
    }
}
