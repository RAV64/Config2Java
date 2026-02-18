package org.msuo.data2java;

import org.w3c.dom.Element;

public final class XmlDeserializer implements Deserializer {

    @Override
    public <T> T deserialize(String source, Class<T> targetClass) {
        return ObjectMapper.deserialize(parse(source), targetClass);
    }

    private static DataValue parse(String source) {
        try {
            Element root = XmlParser.parseRoot(source);
            return XmlDataValueFactory.rootValue(root);
        } catch (Exception e) {
            throw new DataSourceException("XML", "parse", e.getMessage(), e);
        }
    }
}
