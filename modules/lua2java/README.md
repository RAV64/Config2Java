# lua2java

Lua deserializer for Data2Java.

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
class DataModel {
    public String name;
    public Integer port;
}

String lua = "return { name = 'svc', port = 8080 }";
DataModel data = new LuaDeserializer().deserialize(lua, DataModel.class);

assertEquals("svc", data.name);
assertEquals(Integer.valueOf(8080), data.port);
```

## Missing keys keep defaults

```java
class DataModel {
    public String name = "default-name";
    public Integer port;
}

DataModel data = new LuaDeserializer().deserialize("return { port = 8080 }", DataModel.class);
assertEquals("default-name", data.name);
```

## Nested objects

```java
class DataModel {
    public Db db;
    static class Db {
        public String host;
        public Integer port;
    }
}

String lua = "return { db = { host = 'db', port = 15432 } }";
DataModel data = new LuaDeserializer().deserialize(lua, DataModel.class);

assertEquals("db", data.db.host);
assertEquals(Integer.valueOf(15432), data.db.port);
```

## Optionals

```java
import java.util.Optional;

class DataModel {
    public Optional<String> user = Optional.of("default-user");
}

DataModel present = new LuaDeserializer().deserialize("return { user = 'alice' }", DataModel.class);
DataModel missing = new LuaDeserializer().deserialize("return {}", DataModel.class);

assertEquals(Optional.of("alice"), present.user);
assertEquals(Optional.of("default-user"), missing.user);
```

## Collections and maps

```java
import java.util.List;
import java.util.Map;

class DataModel {
    public List<String> tags;
    public Map<String, Integer> limits;
}

String lua = "return { tags = { 'a', 'b' }, limits = { api = 10 } }";
DataModel data = new LuaDeserializer().deserialize(lua, DataModel.class);

assertEquals(List.of("a", "b"), data.tags);
assertEquals(Integer.valueOf(10), data.limits.get("api"));
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
class GenericData {
    public GenericBox<List<GenericItem<String>>> foo;
    public Map<StringConstructedGenericKey<Integer>, List<String>> values;
}

String lua = "return { foo = { value = { { payload = 'a' } } }, values = { k1 = { 'x', 'y' } } }";
GenericData data = new LuaDeserializer().deserialize(lua, GenericData.class);
```

## Class references

```java
interface Service {}
final class ServiceImpl implements Service {}
class DataModel {
    public Class<Service> impl;
}

String lua = "return { impl = '" + ServiceImpl.class.getName() + "' }";
DataModel data = new LuaDeserializer().deserialize(lua, DataModel.class);

assertEquals(ServiceImpl.class, data.impl);
```

## Nil semantics

```java
import java.util.Optional;

class DataModel {
    public Optional<String> user = Optional.of("default-user");
}

DataModel data = new LuaDeserializer().deserialize("return { user = nil }", DataModel.class);

// Lua table `user = nil` removes key, so field behaves as missing.
assertEquals(Optional.of("default-user"), data.user);
```

## Environment and globals

```java
class DataModel {
    public String name;
    public Mode mode;
    enum Mode { DEV, PROD }
}

Map<String, String> env = Map.of("DATA2JAVA_TEST_APP_ENV", "prod");
Map<String, Object> globals = Map.of("defaultName", "worker-default");

// Pass injected environment and global maps to deserialize.

String lua = """
local c = { mode = 'DEV', name = defaultName }
if os.getenv('DATA2JAVA_TEST_APP_ENV') == 'prod' then c.mode = 'PROD' end
return c
""";

DataModel data = new LuaDeserializer().deserialize(lua, DataModel.class, env, globals);
assertEquals(DataModel.Mode.PROD, data.mode);
assertEquals("worker-default", data.name);
```

`os.getenv` resolves from injected values first, then falls back to system environment variables.

See [../../errors.md](../../errors.md) for error details.
