package org.msuo.config2java;

import org.w3c.dom.Element;

public final class XmlDeserializer implements Deserializer {

    private XmlDeserializer() {}

    public static <T> T deserialize(String source, Class<T> configClass) {
        return ObjectMapper.deserialize(parse(source), configClass);
    }

    private static ConfigValue parse(String source) {
        try {
            Element root = XmlParser.parseRoot(source);
            return XmlConfigValueFactory.rootValue(root);
        } catch (Exception e) {
            throw new ConfigSourceException("XML", "parse", e.getMessage(), e);
        }
    }
}
