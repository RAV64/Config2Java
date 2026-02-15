package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class XmlDeserializerTest {

    enum Mode {
        DEV,
        PROD,
    }

    static final class AppCfg {

        public String host;
        public Integer port;
        public Mode mode;
    }

    static final class RootCfg {

        public AppCfg app;
    }

    @Test
    void parsesSimpleXmlTree() {
        String xml =
            "<config><app><host>localhost</host><port>8080</port><mode>PROD</mode></app></config>";

        RootCfg cfg = new XmlDeserializer().deserialize(xml, RootCfg.class);

        assertEquals("localhost", cfg.app.host);
        assertEquals(Integer.valueOf(8080), cfg.app.port);
        assertEquals(Mode.PROD, cfg.app.mode);
    }
}
