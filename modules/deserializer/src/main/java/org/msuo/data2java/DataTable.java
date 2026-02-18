package org.msuo.data2java;

interface DataTable {
    default DataValue getField(String key) {
        return MissingValue.INSTANCE;
    }

    default int length() {
        return 0;
    }

    default DataValue getIndex(int index1Based) {
        return MissingValue.INSTANCE;
    }

    default Iterable<DataEntry> entries() {
        return java.util.Collections::emptyIterator;
    }
}
