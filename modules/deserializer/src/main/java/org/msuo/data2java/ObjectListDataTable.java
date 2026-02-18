package org.msuo.data2java;

import java.util.Iterator;
import java.util.List;

final class ObjectListDataTable implements DataTable {

    private final List<?> list;
    private final ObjectMapDataTable.ValueFactory valueFactory;

    ObjectListDataTable(List<?> list, ObjectMapDataTable.ValueFactory valueFactory) {
        this.list = list;
        this.valueFactory = valueFactory;
    }

    @Override
    public int length() {
        return list.size();
    }

    @Override
    public DataValue getIndex(int index1Based) {
        if (index1Based < 1 || index1Based > list.size()) return MissingValue.INSTANCE;
        return valueFactory.wrap(list.get(index1Based - 1));
    }

    @Override
    public Iterable<DataEntry> entries() {
        return () -> new Iterator<DataEntry>() {
            private int index0 = 0;

            @Override
            public boolean hasNext() {
                return index0 < list.size();
            }

            @Override
            public DataEntry next() {
                int index1 = index0 + 1;
                Object value = list.get(index0);
                index0++;
                return DataEntry.of(
                    valueFactory.wrap(index1),
                    valueFactory.wrap(value),
                    String.valueOf(index1)
                );
            }
        };
    }
}
