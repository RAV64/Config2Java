package org.msuo.config2java;

import java.util.Iterator;
import java.util.List;

final class ObjectListConfigTable implements ConfigTable {

    private final List<?> list;
    private final ObjectMapConfigTable.ValueFactory valueFactory;

    ObjectListConfigTable(List<?> list, ObjectMapConfigTable.ValueFactory valueFactory) {
        this.list = list;
        this.valueFactory = valueFactory;
    }

    @Override
    public int length() {
        return list.size();
    }

    @Override
    public ConfigValue getIndex(int index1Based) {
        if (index1Based < 1 || index1Based > list.size()) return MissingValue.INSTANCE;
        return valueFactory.wrap(list.get(index1Based - 1));
    }

    @Override
    public Iterable<ConfigEntry> entries() {
        return () -> new Iterator<ConfigEntry>() {
            private int index0 = 0;

            @Override
            public boolean hasNext() {
                return index0 < list.size();
            }

            @Override
            public ConfigEntry next() {
                int index1 = index0 + 1;
                Object value = list.get(index0);
                index0++;
                return ConfigEntry.of(
                    valueFactory.wrap(index1),
                    valueFactory.wrap(value),
                    String.valueOf(index1)
                );
            }
        };
    }
}
