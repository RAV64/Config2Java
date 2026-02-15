package org.msuo.config2java;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public final class XmlDeserializer extends TreeDeserializer {

    @Override
    protected ConfigValue parse(String source) {
        try {
            DocumentBuilderFactory f = newFactory();

            Document doc = f.newDocumentBuilder().parse(new InputSource(new StringReader(source)));
            Element root = doc.getDocumentElement();
            return new XmlElementValue(root, false);
        } catch (Exception e) {
            throw new ConfigSourceException("XML", "parse", e.getMessage(), e);
        }
    }

    private static DocumentBuilderFactory newFactory() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(false);
        f.setExpandEntityReferences(false);
        f.setXIncludeAware(false);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return f;
    }

    private static ConfigValue valueForElement(Element element) {
        if (isStructuredElement(element)) {
            return new XmlElementValue(element, true);
        }
        return new XmlScalarValue(coerceScalar(textOnly(element)));
    }

    private static boolean isStructuredElement(Element element) {
        if (element.getAttributes().getLength() > 0) return true;

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) return true;
        }
        return false;
    }

    private static final class XmlElementValue implements ConfigValue {

        private final Element element;
        private final boolean includeTextWhenStructured;

        XmlElementValue(Element element, boolean includeTextWhenStructured) {
            this.element = element;
            this.includeTextWhenStructured = includeTextWhenStructured;
        }

        @Override
        public String typename() {
            return "table";
        }

        @Override
        public boolean isNil() {
            return false;
        }

        @Override
        public boolean isTable() {
            return true;
        }

        @Override
        public ConfigTable asTable() {
            return new XmlElementTable(element, includeTextWhenStructured);
        }

        @Override
        public ScalarValue asScalar() {
            return null;
        }
    }

    private static final class XmlElementListValue implements ConfigValue {

        private final List<Element> elements;

        XmlElementListValue(List<Element> elements) {
            this.elements = elements;
        }

        @Override
        public String typename() {
            return "table";
        }

        @Override
        public boolean isNil() {
            return false;
        }

        @Override
        public boolean isTable() {
            return true;
        }

        @Override
        public ConfigTable asTable() {
            return new XmlElementListTable(elements);
        }

        @Override
        public ScalarValue asScalar() {
            return null;
        }
    }

    private static final class XmlScalarValue implements ConfigValue {

        private final Object value;

        XmlScalarValue(Object value) {
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
        public boolean isTable() {
            return false;
        }

        @Override
        public ConfigTable asTable() {
            throw new IllegalStateException("not a table value");
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

    private static final class XmlElementTable implements ConfigTable {

        private final Map<String, Object> attributes = new LinkedHashMap<>();
        private final Map<String, List<Element>> children = new LinkedHashMap<>();
        private final String structuredText;

        XmlElementTable(Element element, boolean includeTextWhenStructured) {
            NamedNodeMap attrs = element.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Node a = attrs.item(i);
                attributes.put(a.getNodeName(), coerceScalar(a.getNodeValue()));
            }

            NodeList nodes = element.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) continue;
                Element child = (Element) n;
                children.computeIfAbsent(child.getTagName(), k -> new ArrayList<>()).add(child);
            }

            if (includeTextWhenStructured && (!attributes.isEmpty() || !children.isEmpty())) {
                String text = textOnly(element);
                this.structuredText = text.isEmpty() ? null : text;
            } else {
                this.structuredText = null;
            }
        }

        @Override
        public ConfigValue getField(String key) {
            if (attributes.containsKey(key)) {
                return new XmlScalarValue(attributes.get(key));
            }

            List<Element> group = children.get(key);
            if (group != null) {
                if (group.size() == 1) {
                    return valueForElement(group.get(0));
                }
                return new XmlElementListValue(group);
            }

            if ("text".equals(key) && structuredText != null) {
                return new XmlScalarValue(coerceScalar(structuredText));
            }

            return MissingValue.INSTANCE;
        }

        @Override
        public Iterable<ConfigEntry> entries() {
            return () -> new Iterator<ConfigEntry>() {
                private final Iterator<Map.Entry<String, Object>> attrsIt = attributes.entrySet().iterator();
                private final Iterator<Map.Entry<String, List<Element>>> childIt = children.entrySet().iterator();
                private boolean textConsumed;

                @Override
                public boolean hasNext() {
                    return attrsIt.hasNext() || childIt.hasNext() || (!textConsumed && structuredText != null);
                }

                @Override
                public ConfigEntry next() {
                    if (attrsIt.hasNext()) {
                        Map.Entry<String, Object> e = attrsIt.next();
                        return ConfigEntry.of(
                            new XmlScalarValue(e.getKey()),
                            new XmlScalarValue(e.getValue()),
                            e.getKey()
                        );
                    }
                    if (childIt.hasNext()) {
                        Map.Entry<String, List<Element>> e = childIt.next();
                        ConfigValue value = e.getValue().size() == 1
                            ? valueForElement(e.getValue().get(0))
                            : new XmlElementListValue(e.getValue());
                        return ConfigEntry.of(
                            new XmlScalarValue(e.getKey()),
                            value,
                            e.getKey()
                        );
                    }

                    textConsumed = true;
                    return ConfigEntry.of(
                        new XmlScalarValue("text"),
                        new XmlScalarValue(coerceScalar(structuredText)),
                        "text"
                    );
                }
            };
        }
    }

    private static final class XmlElementListTable implements ConfigTable {

        private final List<Element> elements;

        XmlElementListTable(List<Element> elements) {
            this.elements = elements;
        }

        @Override
        public int length() {
            return elements.size();
        }

        @Override
        public ConfigValue getIndex(int index1Based) {
            if (index1Based < 1 || index1Based > elements.size()) return MissingValue.INSTANCE;
            return valueForElement(elements.get(index1Based - 1));
        }

        @Override
        public Iterable<ConfigEntry> entries() {
            return () -> new Iterator<ConfigEntry>() {
                private int index = 1;

                @Override
                public boolean hasNext() {
                    return index <= elements.size();
                }

                @Override
                public ConfigEntry next() {
                    int key = index;
                    ConfigValue value = valueForElement(elements.get(index - 1));
                    index++;
                    return ConfigEntry.of(
                        new XmlScalarValue(key),
                        value,
                        String.valueOf(key)
                    );
                }
            };
        }
    }

    private static String textOnly(Element e) {
        StringBuilder sb = new StringBuilder();
        NodeList nodes = e.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
                sb.append(n.getNodeValue());
            }
        }
        return sb.toString().trim();
    }

    private static Object coerceScalar(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";
        if ("true".equalsIgnoreCase(s)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(s)) return Boolean.FALSE;
        try {
            long l = Long.parseLong(s);
            if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                return (int) l;
            }
            return (double) l;
        } catch (NumberFormatException ignored) {}
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ignored) {}
        return s;
    }
}
