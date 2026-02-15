package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class TomlGenericLibrarySanityTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new TomlDeserializer();
    }


    @Test
    void notHardcodedToOneRootType() {
        RootA a = ok("x = 'ok'", RootA.class);
        RootB b = ok("y = 1", RootB.class);

        assertEquals("ok", a.x.value);
        assertEquals(Integer.valueOf(1), b.y.value);
    }

    @Test
    void iTest() {
        TestMe fixture = ok("x = 'eks'\nints = [1, 2, 3]\n[innie]\nb = 7\n[onnie]\nc = 'k'", TestMe.class);

        assertEquals("eks", fixture.x.toString());
        assertTrue(fixture.ints.contains(new PositiveInteger(1)));
        assertTrue(fixture.ints.contains(new PositiveInteger(2)));
        assertTrue(fixture.ints.contains(new PositiveInteger(3)));
        assertEquals("a", fixture.innie.a);
        assertEquals(7, fixture.innie.b);
        assertEquals("k", fixture.onnie.get().c);
        assertEquals(5, fixture.onnie.get().d);
    }
}
