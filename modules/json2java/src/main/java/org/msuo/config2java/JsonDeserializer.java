package org.msuo.config2java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.Map;

public final class JsonDeserializer extends TreeDeserializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ConfigValue parse(String source) {
        try {
            JsonNode root = MAPPER.readTree(source);
            return new JsonConfigValue(root);
        } catch (Exception e) {
            throw new ConfigSourceException("JSON", "parse", e.getMessage(), e);
        }
    }

    private static final class JsonConfigValue implements ConfigValue {

        private final JsonNode node;

        JsonConfigValue(JsonNode node) {
            this.node = node;
        }

        @Override
        public String typename() {
            if (node == null || node.isNull()) return "nil";
            if (node.isObject() || node.isArray()) return "table";
            if (node.isTextual()) return "string";
            if (node.isBoolean()) return "boolean";
            if (node.isNumber()) return "number";
            return "userdata";
        }

        @Override
        public boolean isNil() {
            return node == null || node.isNull();
        }

        @Override
        public boolean isTable() {
            return node != null && (node.isObject() || node.isArray());
        }

        @Override
        public ConfigTable asTable() {
            if (node == null) throw new IllegalStateException("not a table value");
            return new JsonConfigTable(node);
        }

        @Override
        public ScalarValue asScalar() {
            if (node == null || node.isNull()) return null;
            if (node.isTextual()) return ScalarValue.ofString(node.asText());
            if (node.isBoolean()) return ScalarValue.ofBoolean(node.booleanValue());
            if (node.isNumber()) return ScalarNumbers.fromNumber(node.numberValue());
            return null;
        }
    }

    private static final class JsonConfigTable implements ConfigTable {

        private final JsonNode node;

        JsonConfigTable(JsonNode node) {
            this.node = node;
        }

        @Override
        public ConfigValue getField(String key) {
            if (!node.isObject()) return MissingValue.INSTANCE;
            JsonNode value = node.get(key);
            if (value == null) return MissingValue.INSTANCE;
            return new JsonConfigValue(value);
        }

        @Override
        public int length() {
            if (!node.isArray()) return 0;
            return node.size();
        }

        @Override
        public ConfigValue getIndex(int index1Based) {
            if (!node.isArray()) return MissingValue.INSTANCE;
            if (index1Based < 1 || index1Based > node.size()) return MissingValue.INSTANCE;
            JsonNode value = node.get(index1Based - 1);
            if (value == null) return MissingValue.INSTANCE;
            return new JsonConfigValue(value);
        }

        @Override
        public Iterable<ConfigEntry> entries() {
            if (node.isObject()) return objectEntries();
            if (node.isArray()) return arrayEntries();
            return java.util.Collections::emptyIterator;
        }

        private Iterable<ConfigEntry> objectEntries() {
            return () -> new Iterator<ConfigEntry>() {
                private final Iterator<Map.Entry<String, JsonNode>> it = node.properties().iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public ConfigEntry next() {
                    Map.Entry<String, JsonNode> e = it.next();
                    return ConfigEntry.of(
                        new JsonScalarValue(e.getKey()),
                        new JsonConfigValue(e.getValue()),
                        e.getKey()
                    );
                }
            };
        }

        private Iterable<ConfigEntry> arrayEntries() {
            return () -> new Iterator<ConfigEntry>() {
                private int index = 1;

                @Override
                public boolean hasNext() {
                    return index <= node.size();
                }

                @Override
                public ConfigEntry next() {
                    int key = index;
                    JsonNode value = node.get(index - 1);
                    index++;
                    return ConfigEntry.of(
                        new JsonScalarValue(key),
                        new JsonConfigValue(value),
                        String.valueOf(key)
                    );
                }
            };
        }
    }

    private static final class JsonScalarValue implements ConfigValue {

        private final Object value;

        JsonScalarValue(Object value) {
            this.value = value;
        }

        @Override
        public String typename() {
            if (value == null) return "nil";
            if (value instanceof CharSequence) return "string";
            if (value instanceof Boolean) return "boolean";
            if (value instanceof Number) return "number";
            return "userdata";
        }

        @Override
        public boolean isNil() {
            return value == null;
        }

        @Override
        public ScalarValue asScalar() {
            if (value == null) return null;
            if (value instanceof CharSequence) return ScalarValue.ofString(value.toString());
            if (value instanceof Boolean) return ScalarValue.ofBoolean((Boolean) value);
            if (value instanceof Number) return ScalarNumbers.fromNumber((Number) value);
            return null;
        }
    }
}
