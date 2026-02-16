# json2java

JSON deserializer for Config2Java.

Depends on:
- [../deserializer](../deserializer/README.md)
- `com.fasterxml.jackson.core:jackson-databind`

```gradle
implementation "org.msuo:json2java:<version>"
```

## Leaf values

```java
class Cfg {
    public String name;
    public Integer port;
}

String json = "{\"name\":\"svc\",\"port\":8080}";
Cfg cfg = new JsonDeserializer().deserialize(json, Cfg.class);

assertEquals("svc", cfg.name);
assertEquals(Integer.valueOf(8080), cfg.port);
```

## Missing keys keep defaults

```java
class Cfg {
    public String name = "default-name";
    public Integer port;
}

Cfg cfg = new JsonDeserializer().deserialize("{\"port\":8080}", Cfg.class);
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

String json = "{\"db\":{\"host\":\"db\",\"port\":15432}}";
Cfg cfg = new JsonDeserializer().deserialize(json, Cfg.class);

assertEquals("db", cfg.db.host);
assertEquals(Integer.valueOf(15432), cfg.db.port);
```

## Optionals

```java
import java.util.Optional;

class Cfg {
    public Optional<String> user = Optional.of("default-user");
}

Cfg present = new JsonDeserializer().deserialize("{\"user\":\"alice\"}", Cfg.class);
Cfg missing = new JsonDeserializer().deserialize("{}", Cfg.class);

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

String json = "{\"tags\":[\"a\",\"b\"],\"limits\":{\"api\":10}}";
Cfg cfg = new JsonDeserializer().deserialize(json, Cfg.class);

assertEquals(List.of("a", "b"), cfg.tags);
assertEquals(Integer.valueOf(10), cfg.limits.get("api"));
```

## Null semantics

```java
import java.util.Optional;

class Cfg {
    public Optional<String> user = Optional.of("default-user");
}

Cfg cfg = new JsonDeserializer().deserialize("{\"user\":null}", Cfg.class);
assertEquals(Optional.empty(), cfg.user);
```

See [../../errors.md](../../errors.md) for error details.
