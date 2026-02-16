package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class LuaGenericLibrarySanityTest extends LuaContractSupport {

    @Test
    void notHardcodedToOneRootType() {
        RootA a = ok("return { x = 'ok' }", RootA.class);
        RootB b = ok("return { y = 1 }", RootB.class);

        assertEquals("ok", a.x.value);
        assertEquals(Integer.valueOf(1), b.y.value);
    }

    @Test
    void iTest() {
        TestMe fixture = ok("return { x = 'eks', ints = {1,2,3}, innie = { b = 7 }, onnie = { c = 'k' } }", TestMe.class);

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
