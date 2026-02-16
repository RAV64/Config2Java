# xml2java

XML deserializer for Config2Java.

Depends on:
- [../deserializer](../deserializer/README.md)
- no extra runtime dependency (JDK XML parser)

```gradle
implementation "org.msuo:xml2java:<version>"
```

## Leaf values

```java
class Cfg {
    public String name;
    public Integer port;
}

String xml = "<config><name>svc</name><port>8080</port></config>";
Cfg cfg = new XmlDeserializer().deserialize(xml, Cfg.class);

assertEquals("svc", cfg.name);
assertEquals(Integer.valueOf(8080), cfg.port);
```

## Missing keys keep defaults

```java
class Cfg {
    public String name = "default-name";
    public Integer port;
}

Cfg cfg = new XmlDeserializer().deserialize("<config><port>8080</port></config>", Cfg.class);
assertEquals("default-name", cfg.name);
```

## Nested objects

```java
class Cfg {
    public Db db;
    static class Db {
        public String host;
        public Integer port;
    }
}

String xml = "<config><db><host>db</host><port>15432</port></db></config>";
Cfg cfg = new XmlDeserializer().deserialize(xml, Cfg.class);

assertEquals("db", cfg.db.host);
assertEquals(Integer.valueOf(15432), cfg.db.port);
```

## Optionals

```java
import java.util.Optional;

class Cfg {
    public Optional<String> user = Optional.of("default-user");
}

Cfg present = new XmlDeserializer().deserialize("<config><user>alice</user></config>", Cfg.class);
Cfg missing = new XmlDeserializer().deserialize("<config/>", Cfg.class);

assertEquals(Optional.of("alice"), present.user);
assertEquals(Optional.of("default-user"), missing.user);
```

## Collections and maps

```java
import java.util.List;
import java.util.Map;

class Cfg {
    public List<String> tags;
    public Map<String, Integer> limits;
}

String xml = "<config><tags>a</tags><tags>b</tags><limits><api>10</api></limits></config>";
Cfg cfg = new XmlDeserializer().deserialize(xml, Cfg.class);

assertEquals(List.of("a", "b"), cfg.tags);
assertEquals(Integer.valueOf(10), cfg.limits.get("api"));
```

## Optional semantics

```java
import java.util.Optional;

class Cfg {
    public Optional<String> user = Optional.of("default-user");
}

// XML has no explicit null literal. Omitted key behaves as missing.
Cfg cfg = new XmlDeserializer().deserialize("<config/>", Cfg.class);
assertEquals(Optional.of("default-user"), cfg.user);
```

See [../../errors.md](../../errors.md) for error details.
