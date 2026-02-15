package org.msuo.config2java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Iterator;
import java.util.Map;

public final class JsonDeserializer extends TreeDeserializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ConfigValue parse(String source) {
        try {
            JsonNode root = MAPPER.readTree(source);
            return new JsonConfigValue(root);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + e.getMessage(), e);
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
        public boolean isMissing() {
            return false;
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
            if (node.isIntegralNumber()) {
                long value = node.longValue();
                if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                    return ScalarValue.ofInt((int) value);
                }
                return ScalarValue.ofDouble((double) value);
            }
            if (node.isFloatingPointNumber()) return ScalarValue.ofDouble(node.doubleValue());
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
            if (value == null || value.isNull()) return MissingValue.INSTANCE;
            return new JsonConfigValue(value);
        }

        @Override
        public Iterable<ConfigEntry> entries() {
            if (node.isObject()) {
                return () -> new Iterator<ConfigEntry>() {
                    private final Iterator<Map.Entry<String, JsonNode>> it = node.fields();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                @Override
                public ConfigEntry next() {
                    Map.Entry<String, JsonNode> e = it.next();
                    return ConfigEntry.of(
                        new JsonConfigValue(TextNode.valueOf(e.getKey())),
                        new JsonConfigValue(e.getValue()),
                        e.getKey()
                    );
                }
            };
        }

            if (node.isArray()) {
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
                        new JsonConfigValue(IntNode.valueOf(key)),
                        new JsonConfigValue(value),
                        String.valueOf(key)
                    );
                }
            };
        }

        return java.util.Collections::emptyIterator;
    }
    }
}
