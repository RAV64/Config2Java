package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlGenericLibrarySanityTest extends SharedContractSupport {
    @Override
    protected Deserializer deserializer() {
        return new XmlDeserializer();
    }


    @Test
    void notHardcodedToOneRootType() {
        RootA a = ok("<config><x>ok</x></config>", RootA.class);
        RootB b = ok("<config><y>1</y></config>", RootB.class);

        assertEquals("ok", a.x.value);
        assertEquals(Integer.valueOf(1), b.y.value);
    }

    @Test
    void iTest() {
        TestMe fixture = ok("<config><x>eks</x><ints>1</ints><ints>2</ints><ints>3</ints><innie><b>7</b></innie><onnie><c>k</c></onnie></config>", TestMe.class);

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
