package org.msuo.config2java;

interface ConfigTable {
    default ConfigValue getField(String key) {
        return MissingValue.INSTANCE;
    }

    default int length() {
        return 0;
    }

    default ConfigValue getIndex(int index1Based) {
        return MissingValue.INSTANCE;
    }

    default Iterable<ConfigEntry> entries() {
        return java.util.Collections::emptyIterator;
    }
}
