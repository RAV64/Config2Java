# groovy2java

Groovy deserializer for Config2Java.

Depends on:
- [../deserializer](../deserializer/README.md)
- `org.apache.groovy:groovy`

## Leaf values

```java
class Cfg {
    public String name;
    public Integer port;
}

String groovy = "return [name: 'svc', port: 8080]";
Cfg cfg = new GroovyDeserializer().deserialize(groovy, Cfg.class);

assertEquals("svc", cfg.name);
assertEquals(Integer.valueOf(8080), cfg.port);
```

## Missing keys keep defaults

```java
class Cfg {
    public String name = "default-name";
    public Integer port;
}

Cfg cfg = new GroovyDeserializer().deserialize("return [port: 8080]", Cfg.class);
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

String groovy = "return [db: [host: 'db', port: 15432]]";
Cfg cfg = new GroovyDeserializer().deserialize(groovy, Cfg.class);

assertEquals("db", cfg.db.host);
assertEquals(Integer.valueOf(15432), cfg.db.port);
```

## Optionals

```java
import java.util.Optional;

class Cfg {
    public Optional<String> user = Optional.of("default-user");
}

Cfg present = new GroovyDeserializer().deserialize("return [user: 'alice']", Cfg.class);
Cfg missing = new GroovyDeserializer().deserialize("return [:]", Cfg.class);

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

String groovy = "return [tags: ['a', 'b'], limits: [api: 10]]";
Cfg cfg = new GroovyDeserializer().deserialize(groovy, Cfg.class);

assertEquals(List.of("a", "b"), cfg.tags);
assertEquals(Integer.valueOf(10), cfg.limits.get("api"));
```

## Null semantics

```java
import java.util.Optional;

class Cfg {
    public Optional<String> user = Optional.of("default-user");
}

Cfg cfg = new GroovyDeserializer().deserialize("return [user: null]", Cfg.class);
assertEquals(Optional.empty(), cfg.user);
```

## Environment and globals

```java
class Cfg {
    public String name;
    public Mode mode;
    enum Mode { DEV, PROD }
}

ScriptBindings bindings = ScriptBindings.builder()
    .env("CONFIG2JAVA_TEST_APP_ENV", "prod")
    .global("defaultName", "worker-default")
    .build();

GroovyDeserializer d = new GroovyDeserializer(bindings);

String groovy = """
def c = [mode: 'DEV', name: defaultName]
if (ENV.CONFIG2JAVA_TEST_APP_ENV == 'prod') c.mode = 'PROD'
return c
""";

Cfg cfg = d.deserialize(groovy, Cfg.class);
assertEquals(Cfg.Mode.PROD, cfg.mode);
assertEquals("worker-default", cfg.name);
```

`ENV` resolves from injected values first, then falls back to system environment variables.

See [../../errors.md](../../errors.md) for error details.
