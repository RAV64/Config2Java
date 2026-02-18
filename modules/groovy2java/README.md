# groovy2java

Groovy deserializer for Data2Java.

Depends on:
- [../deserializer](../deserializer/README.md)
- `org.apache.groovy:groovy`

```gradle
implementation "org.msuo:groovy2java:<version>"
```

Supported Groovy versions:
- Groovy 5.0 and above (tested with `org.apache.groovy:groovy:5.0.4`)

## Leaf values

```java
class DataModel {
    public String name;
    public Integer port;
}

String groovy = "return [name: 'svc', port: 8080]";
DataModel data = new GroovyDeserializer().deserialize(groovy, DataModel.class);

assertEquals("svc", data.name);
assertEquals(Integer.valueOf(8080), data.port);
```

## Missing keys keep defaults

```java
class DataModel {
    public String name = "default-name";
    public Integer port;
}

DataModel data = new GroovyDeserializer().deserialize("return [port: 8080]", DataModel.class);
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

String groovy = "return [db: [host: 'db', port: 15432]]";
DataModel data = new GroovyDeserializer().deserialize(groovy, DataModel.class);

assertEquals("db", data.db.host);
assertEquals(Integer.valueOf(15432), data.db.port);
```

## Optionals

```java
import java.util.Optional;

class DataModel {
    public Optional<String> user = Optional.of("default-user");
}

DataModel present = new GroovyDeserializer().deserialize("return [user: 'alice']", DataModel.class);
DataModel missing = new GroovyDeserializer().deserialize("return [:]", DataModel.class);

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

String groovy = "return [tags: ['a', 'b'], limits: [api: 10]]";
DataModel data = new GroovyDeserializer().deserialize(groovy, DataModel.class);

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

String groovy = "return [foo: [value: [[payload: 'a']]], values: [k1: ['x', 'y']]]";
GenericData data = new GroovyDeserializer().deserialize(groovy, GenericData.class);
```

## Class references

```java
interface Service {}
final class ServiceImpl implements Service {}
class DataModel {
    public Class<Service> impl;
}

String groovy = "return [impl: '" + ServiceImpl.class.getName() + "']";
DataModel data = new GroovyDeserializer().deserialize(groovy, DataModel.class);

assertEquals(ServiceImpl.class, data.impl);
```

## Null semantics

```java
import java.util.Optional;

class DataModel {
    public Optional<String> user = Optional.of("default-user");
}

DataModel data = new GroovyDeserializer().deserialize("return [user: null]", DataModel.class);
assertEquals(Optional.empty(), data.user);
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

String groovy = """
def c = [mode: 'DEV', name: defaultName]
if (ENV.DATA2JAVA_TEST_APP_ENV == 'prod') c.mode = 'PROD'
return c
""";

DataModel data = new GroovyDeserializer().deserialize(groovy, DataModel.class, env, globals);
assertEquals(DataModel.Mode.PROD, data.mode);
assertEquals("worker-default", data.name);
```

`ENV` resolves from injected values first, then falls back to system environment variables.

## File loading

When deserializing from a Groovy file path, scripts can load other Groovy files with:

```groovy
load('foo.groovy')
load('../bar.groovy')
load('./folder/baz.groovy')
load('/absolute/path/to/file.groovy')
```

Relative paths resolve from the currently executing Groovy file. Nested loads resolve relative to the importing file.
Loaded files run with the same `ENV` and globals as the calling script.

```groovy
// main.groovy
return load('./child.groovy')
```

```groovy
// child.groovy
def mode = (ENV.DATA2JAVA_TEST_APP_ENV == 'prod') ? 'PROD' : 'DEV'
return [mode: mode, name: defaultName]
```

See [../../errors.md](../../errors.md) for error details.
