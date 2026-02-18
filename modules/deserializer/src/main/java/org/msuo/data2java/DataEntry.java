package org.msuo.data2java;

final class DataEntry {

    private final DataValue key;
    private final DataValue value;
    private final String rawKeyString;

    private DataEntry(DataValue key, DataValue value, String rawKeyString) {
        this.key = key;
        this.value = value;
        this.rawKeyString = rawKeyString;
    }

    static DataEntry of(DataValue key, DataValue value, String rawKeyString) {
        return new DataEntry(key, value, rawKeyString);
    }

    DataValue key() {
        return key;
    }

    DataValue value() {
        return value;
    }

    String rawKeyString() {
        return rawKeyString;
    }
}
