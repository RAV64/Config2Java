package org.msuo.data2java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.Map;

public final class JsonDeserializer implements Deserializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public <T> T deserialize(String source, Class<T> targetClass) {
        return org.msuo.data2java.ObjectMapper.deserialize(parse(source), targetClass);
    }

    private static DataValue parse(String source) {
        try {
            JsonNode root = MAPPER.readTree(source);
            return new JsonDataValue(root);
        } catch (Exception e) {
            throw new DataSourceException("JSON", "parse", e.getMessage(), e);
        }
    }

    private static final class JsonDataValue implements DataValue {

        private final JsonNode node;

        JsonDataValue(JsonNode node) {
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
        public DataTable asTable() {
            if (node == null) throw new IllegalStateException("not a table value");
            return new JsonDataTable(node);
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

    private static final class JsonDataTable implements DataTable {

        private final JsonNode node;

        JsonDataTable(JsonNode node) {
            this.node = node;
        }

        @Override
        public DataValue getField(String key) {
            if (!node.isObject()) return MissingValue.INSTANCE;
            JsonNode value = node.get(key);
            if (value == null) return MissingValue.INSTANCE;
            return new JsonDataValue(value);
        }

        @Override
        public int length() {
            if (!node.isArray()) return 0;
            return node.size();
        }

        @Override
        public DataValue getIndex(int index1Based) {
            if (!node.isArray()) return MissingValue.INSTANCE;
            if (index1Based < 1 || index1Based > node.size()) return MissingValue.INSTANCE;
            JsonNode value = node.get(index1Based - 1);
            if (value == null) return MissingValue.INSTANCE;
            return new JsonDataValue(value);
        }

        @Override
        public Iterable<DataEntry> entries() {
            if (node.isObject()) return objectEntries();
            if (node.isArray()) return arrayEntries();
            return java.util.Collections::emptyIterator;
        }

        private Iterable<DataEntry> objectEntries() {
            return () -> new Iterator<DataEntry>() {
                private final Iterator<Map.Entry<String, JsonNode>> it = node.properties().iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public DataEntry next() {
                    Map.Entry<String, JsonNode> e = it.next();
                    return DataEntry.of(
                        new JavaScalarDataValue(e.getKey()),
                        new JsonDataValue(e.getValue()),
                        e.getKey()
                    );
                }
            };
        }

        private Iterable<DataEntry> arrayEntries() {
            return () -> new Iterator<DataEntry>() {
                private int index = 1;

                @Override
                public boolean hasNext() {
                    return index <= node.size();
                }

                @Override
                public DataEntry next() {
                    int key = index;
                    JsonNode value = node.get(index - 1);
                    index++;
                    return DataEntry.of(
                        new JavaScalarDataValue(key),
                        new JsonDataValue(value),
                        String.valueOf(key)
                    );
                }
            };
        }
    }
}
