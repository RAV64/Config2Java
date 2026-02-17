# toml2java

TOML deserializer for Config2Java.

Depends on:
- [../deserializer](../deserializer/README.md)
- `org.tomlj:tomlj`

```gradle
implementation "org.msuo:toml2java:<version>"
```

## Leaf values

```java
class Cfg {
    public String name;
    public Integer port;
}

String toml = """
name = "svc"
port = 8080
""";
Cfg cfg = new TomlDeserializer().deserialize(toml, Cfg.class);

assertEquals("svc", cfg.name);
assertEquals(Integer.valueOf(8080), cfg.port);
```

## Missing keys keep defaults

```java
class Cfg {
    public String name = "default-name";
    public Integer port;
}

Cfg cfg = new TomlDeserializer().deserialize("port = 8080", Cfg.class);
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

String toml = """
[db]
host = "db"
port = 15432
""";
Cfg cfg = new TomlDeserializer().deserialize(toml, Cfg.class);

assertEquals("db", cfg.db.host);
assertEquals(Integer.valueOf(15432), cfg.db.port);
```

## Optionals

```java
import java.util.Optional;

class Cfg {
    public Optional<String> user = Optional.of("default-user");
}

Cfg present = new TomlDeserializer().deserialize("user = 'alice'", Cfg.class);
Cfg missing = new TomlDeserializer().deserialize("", Cfg.class);

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

String toml = """
tags = ["a", "b"]

[limits]
api = 10
""";
Cfg cfg = new TomlDeserializer().deserialize(toml, Cfg.class);

assertEquals(List.of("a", "b"), cfg.tags);
assertEquals(Integer.valueOf(10), cfg.limits.get("api"));
```

## Nested generics

```java
import java.util.List;
import java.util.Map;

class GenericBox<T> { public T value; }
class GenericItem<T> { public T payload; }
class StringConstructedGenericKey<T> {
    public final String value;
    public StringConstructedGenericKey(String value) { this.value = value; }
}
class GenericConfig {
    public GenericBox<List<GenericItem<String>>> foo;
    public Map<StringConstructedGenericKey<Integer>, List<String>> values;
}

String toml = """
[[foo.value]]
payload = "a"

[values]
k1 = ["x", "y"]
""";
GenericConfig cfg = new TomlDeserializer().deserialize(toml, GenericConfig.class);
```

## Optional semantics

```java
import java.util.Optional;

class Cfg {
    public Optional<String> user = Optional.of("default-user");
}

// TOML has no explicit null literal. Omitted key behaves as missing.
Cfg cfg = new TomlDeserializer().deserialize("", Cfg.class);
assertEquals(Optional.of("default-user"), cfg.user);
```

See [../../errors.md](../../errors.md) for error details.
