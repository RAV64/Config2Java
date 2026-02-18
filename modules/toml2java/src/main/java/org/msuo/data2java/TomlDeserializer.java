package org.msuo.data2java;

import java.util.Iterator;
import java.util.stream.Collectors;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

public final class TomlDeserializer implements Deserializer {

    @Override
    public <T> T deserialize(String source, Class<T> targetClass) {
        return ObjectMapper.deserialize(parse(source), targetClass);
    }

    private static DataValue parse(String source) {
        TomlParseResult result = Toml.parse(source);
        if (result.hasErrors()) {
            String details = result
                .errors()
                .stream()
                .map(e -> e.position() + " " + e.getMessage())
                .collect(Collectors.joining("; "));
            throw new DataSourceException("TOML", "parse", details, null);
        }

        return new TomlDataValue(result);
    }

    private static final class TomlDataValue implements DataValue {

        private final Object value;

        TomlDataValue(Object value) {
            this.value = value;
        }

        @Override
        public String typename() {
            if (value instanceof TomlParseResult || value instanceof TomlTable || value instanceof TomlArray) {
                return "table";
            }
            return JavaScalarDataValue.typenameOf(value);
        }

        @Override
        public boolean isNil() {
            return value == null;
        }

        @Override
        public boolean isTable() {
            return value instanceof TomlParseResult || value instanceof TomlTable || value instanceof TomlArray;
        }

        @Override
        public DataTable asTable() {
            if (value instanceof TomlParseResult) {
                return new TomlMapTable((TomlParseResult) value);
            }
            if (value instanceof TomlTable) {
                return new TomlMapTable((TomlTable) value);
            }
            if (value instanceof TomlArray) {
                return new TomlListTable((TomlArray) value);
            }
            throw new IllegalStateException("not a table value");
        }

        @Override
        public ScalarValue asScalar() {
            return JavaScalarDataValue.scalarOf(value);
        }
    }

    private static final class TomlMapTable implements DataTable {

        private final TomlTable table;

        TomlMapTable(TomlTable table) {
            this.table = table;
        }

        @Override
        public DataValue getField(String key) {
            if (!table.keySet().contains(key)) return MissingValue.INSTANCE;
            return new TomlDataValue(table.get(key));
        }

        @Override
        public Iterable<DataEntry> entries() {
            return () -> new Iterator<DataEntry>() {
                private final Iterator<String> keys = table.keySet().iterator();

                @Override
                public boolean hasNext() {
                    return keys.hasNext();
                }

                @Override
                public DataEntry next() {
                    String key = keys.next();
                    return DataEntry.of(
                        new JavaScalarDataValue(key),
                        new TomlDataValue(table.get(key)),
                        key
                    );
                }
            };
        }
    }

    private static final class TomlListTable implements DataTable {

        private final TomlArray array;

        TomlListTable(TomlArray array) {
            this.array = array;
        }

        @Override
        public int length() {
            return array.size();
        }

        @Override
        public DataValue getIndex(int index1Based) {
            if (index1Based < 1 || index1Based > array.size()) return MissingValue.INSTANCE;
            Object value = array.get(index1Based - 1);
            return new TomlDataValue(value);
        }

        @Override
        public Iterable<DataEntry> entries() {
            return () -> new Iterator<DataEntry>() {
                private int index = 1;

                @Override
                public boolean hasNext() {
                    return index <= array.size();
                }

                @Override
                public DataEntry next() {
                    int key = index;
                    Object value = array.get(index - 1);
                    index++;
                    return DataEntry.of(
                        new JavaScalarDataValue(key),
                        new TomlDataValue(value),
                        String.valueOf(key)
                    );
                }
            };
        }
    }
}
