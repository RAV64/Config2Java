package org.msuo.config2java;

import org.w3c.dom.Element;

public final class XmlDeserializer extends TreeDeserializer {

    @Override
    protected ConfigValue parse(String source) {
        try {
            Element root = XmlParser.parseRoot(source);
            return XmlConfigValueFactory.rootValue(root);
        } catch (Exception e) {
            throw new ConfigSourceException("XML", "parse", e.getMessage(), e);
        }
    }
}
