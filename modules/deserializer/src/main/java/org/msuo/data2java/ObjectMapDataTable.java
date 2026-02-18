package org.msuo.data2java;

import java.util.Iterator;
import java.util.Map;

final class ObjectMapDataTable implements DataTable {

    interface ValueFactory {
        DataValue wrap(Object value);
    }

    private final Map<?, ?> map;
    private final ValueFactory valueFactory;

    ObjectMapDataTable(Map<?, ?> map, ValueFactory valueFactory) {
        this.map = map;
        this.valueFactory = valueFactory;
    }

    @Override
    public DataValue getField(String key) {
        if (!map.containsKey(key)) return MissingValue.INSTANCE;
        return valueFactory.wrap(map.get(key));
    }

    @Override
    public Iterable<DataEntry> entries() {
        return () -> new Iterator<DataEntry>() {
            private final Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public DataEntry next() {
                Map.Entry<?, ?> e = it.next();
                DataValue key = e.getKey() == null
                    ? MissingValue.INSTANCE
                    : valueFactory.wrap(e.getKey());
                return DataEntry.of(
                    key,
                    valueFactory.wrap(e.getValue()),
                    String.valueOf(e.getKey())
                );
            }
        };
    }
}
