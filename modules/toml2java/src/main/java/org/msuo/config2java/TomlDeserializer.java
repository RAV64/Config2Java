package org.msuo.config2java;

import java.util.Iterator;
import java.util.stream.Collectors;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

public final class TomlDeserializer implements Deserializer {

    private TomlDeserializer() {}

    @Override
    public <T> T deserialize(CharSequence source, Class<T> configClass) {
        return deserialize(source.toString(), configClass);
    }

    public static <T> T deserialize(String source, Class<T> configClass) {
        return ObjectMapper.deserialize(parse(source), configClass);
    }

    private static ConfigValue parse(String source) {
        TomlParseResult result = Toml.parse(source);
        if (result.hasErrors()) {
            String details = result
                .errors()
                .stream()
                .map(e -> e.position() + " " + e.getMessage())
                .collect(Collectors.joining("; "));
            throw new ConfigSourceException("TOML", "parse", details, null);
        }

        return new TomlConfigValue(result);
    }

    private static final class TomlConfigValue implements ConfigValue {

        private final Object value;

        TomlConfigValue(Object value) {
            this.value = value;
        }

        @Override
        public String typename() {
            if (value instanceof TomlParseResult || value instanceof TomlTable || value instanceof TomlArray) {
                return "table";
            }
            return JavaScalarConfigValue.typenameOf(value);
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
        public ConfigTable asTable() {
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
            return JavaScalarConfigValue.scalarOf(value);
        }
    }

    private static final class TomlMapTable implements ConfigTable {

        private final TomlTable table;

        TomlMapTable(TomlTable table) {
            this.table = table;
        }

        @Override
        public ConfigValue getField(String key) {
            if (!table.keySet().contains(key)) return MissingValue.INSTANCE;
            return new TomlConfigValue(table.get(key));
        }

        @Override
        public Iterable<ConfigEntry> entries() {
            return () -> new Iterator<ConfigEntry>() {
                private final Iterator<String> keys = table.keySet().iterator();

                @Override
                public boolean hasNext() {
                    return keys.hasNext();
                }

                @Override
                public ConfigEntry next() {
                    String key = keys.next();
                    return ConfigEntry.of(
                        new JavaScalarConfigValue(key),
                        new TomlConfigValue(table.get(key)),
                        key
                    );
                }
            };
        }
    }

    private static final class TomlListTable implements ConfigTable {

        private final TomlArray array;

        TomlListTable(TomlArray array) {
            this.array = array;
        }

        @Override
        public int length() {
            return array.size();
        }

        @Override
        public ConfigValue getIndex(int index1Based) {
            if (index1Based < 1 || index1Based > array.size()) return MissingValue.INSTANCE;
            Object value = array.get(index1Based - 1);
            return new TomlConfigValue(value);
        }

        @Override
        public Iterable<ConfigEntry> entries() {
            return () -> new Iterator<ConfigEntry>() {
                private int index = 1;

                @Override
                public boolean hasNext() {
                    return index <= array.size();
                }

                @Override
                public ConfigEntry next() {
                    int key = index;
                    Object value = array.get(index - 1);
                    index++;
                    return ConfigEntry.of(
                        new JavaScalarConfigValue(key),
                        new TomlConfigValue(value),
                        String.valueOf(key)
                    );
                }
            };
        }
    }
}
