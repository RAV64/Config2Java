package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CoreLeafParsingTest extends CoreMapperContractSupport {

    @Test
    void leafString_success() {
        StringLeaf cfg = ok(obj("name", "ok"), StringLeaf.class);
        assertEquals("ok", cfg.name.value);
    }

    @Test
    void leafString_invalid_reportsCtorRejected() {
        ConfigDeserializationException ex = fails(obj("name", ""), StringLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "name");
    }

    @Test
    void leafInteger_success() {
        IntLeaf cfg = ok(obj("n", 3), IntLeaf.class);
        assertEquals(Integer.valueOf(3), cfg.n.value);
    }

    @Test
    void leafInteger_invalid_reportsCtorRejected() {
        ConfigDeserializationException ex = fails(obj("n", 0), IntLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.CtorRejected.class, "n");
    }

    @Test
    void enum_success() {
        EnumLeaf cfg = ok(obj("mode", "PROD"), EnumLeaf.class);
        assertEquals(Mode.PROD, cfg.mode);
    }

    @Test
    void enum_unknown_reportsEnumUnknown() {
        ConfigDeserializationException ex = fails(obj("mode", "NOPE"), EnumLeaf.class);
        assertSingleError(ex, ConfigErrorTypes.EnumUnknown.class, "mode");
    }
}
