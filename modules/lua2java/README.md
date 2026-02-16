# lua2java

Lua deserializer for Config2Java.

Depends on:
- [../deserializer](../deserializer/README.md)
- `org.luaj:luaj-jse`

```gradle
implementation "org.msuo:lua2java:<version>"
```

Supported Lua versions:
- Lua 5.2 and above (LuaJ provides Lua 5.2 semantics via `luaj-jse:3.0.1`)

## Leaf values

```java
class Cfg {
    public String name;
    public Integer port;
}

String lua = "return { name = 'svc', port = 8080 }";
Cfg cfg = LuaDeserializer.deserialize(lua, Cfg.class);

assertEquals("svc", cfg.name);
assertEquals(Integer.valueOf(8080), cfg.port);
```

## Missing keys keep defaults

```java
class Cfg {
    public String name = "default-name";
    public Integer port;
}

Cfg cfg = LuaDeserializer.deserialize("return { port = 8080 }", Cfg.class);
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

String lua = "return { db = { host = 'db', port = 15432 } }";
Cfg cfg = LuaDeserializer.deserialize(lua, Cfg.class);

assertEquals("db", cfg.db.host);
assertEquals(Integer.valueOf(15432), cfg.db.port);
```

## Optionals

```java
import java.util.Optional;

class Cfg {
    public Optional<String> user = Optional.of("default-user");
}

Cfg present = LuaDeserializer.deserialize("return { user = 'alice' }", Cfg.class);
Cfg missing = LuaDeserializer.deserialize("return {}", Cfg.class);

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

String lua = "return { tags = { 'a', 'b' }, limits = { api = 10 } }";
Cfg cfg = LuaDeserializer.deserialize(lua, Cfg.class);

assertEquals(List.of("a", "b"), cfg.tags);
assertEquals(Integer.valueOf(10), cfg.limits.get("api"));
```

## Nil semantics

```java
import java.util.Optional;

class Cfg {
    public Optional<String> user = Optional.of("default-user");
}

Cfg cfg = LuaDeserializer.deserialize("return { user = nil }", Cfg.class);

// Lua table `user = nil` removes key, so field behaves as missing.
assertEquals(Optional.of("default-user"), cfg.user);
```

## Environment and globals

```java
class Cfg {
    public String name;
    public Mode mode;
    enum Mode { DEV, PROD }
}

Map<String, String> env = Map.of("CONFIG2JAVA_TEST_APP_ENV", "prod");
Map<String, Object> globals = Map.of("defaultName", "worker-default");

// Pass injected environment and global maps to the static call.

String lua = """
local c = { mode = 'DEV', name = defaultName }
if os.getenv('CONFIG2JAVA_TEST_APP_ENV') == 'prod' then c.mode = 'PROD' end
return c
""";

Cfg cfg = LuaDeserializer.deserialize(lua, Cfg.class, env, globals);
assertEquals(Cfg.Mode.PROD, cfg.mode);
assertEquals("worker-default", cfg.name);
```

`os.getenv` resolves from injected values first, then falls back to system environment variables.

See [../../errors.md](../../errors.md) for error details.
