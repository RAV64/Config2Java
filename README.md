# Data2Java

Data2Java is a data-to-Java deserializer that maps data into validated **null-safe** Java objects.

This repository is a monorepo with one core module and format-specific modules. You can depend on exactly the formats you need.

## Modules

- [modules/deserializer](modules/deserializer/README.md): core object-mapping/validation engine
- [modules/lua2java](modules/lua2java/README.md): Lua to Java deserializer
- [modules/groovy2java](modules/groovy2java/README.md): Groovy to Java deserializer
- [modules/toml2java](modules/toml2java/README.md): TOML to Java deserializer
- [modules/json2java](modules/json2java/README.md): JSON to Java deserializer
- [modules/xml2java](modules/xml2java/README.md): XML to Java deserializer
- [errors.md](errors.md): shared error variants and troubleshooting

## Dependency model

Each language module depends on `deserializer` only.

If you depend only on `lua2java`, you do not pull `groovy2java`, `toml2java`, `json2java`, or `xml2java`.

## Add a module

Example (Gradle):

```gradle
dependencies {
    implementation "org.msuo:lua2java:<version>"
}
```

Pick one or multiple modules the same way.

## Deserialization target classes

Use mutable classes with fields that can be set reflectively. Field visibility can be `public`, `protected`, package-private, or `private`.

### Required structure

- Root type and nested object types should have a no-arg constructor.
- Fields should be non-primitive boxed/object types (`Integer`, not `int`).
- Value objects can validate with a single-arg constructor.
- `Optional<T>`, `List<T>`, `Set<T>`, and `Map<K,V>` are supported.
- `Class<T>` is supported: provide a class name string and it resolves if assignable to `T`.
- Nested generic combinations are supported (for example `GenericBox<List<GenericItem<String>>>`, `Map<StringConstructedGenericKey<Integer>, List<String>>`).
- Enums are parsed from string names.
- Expose values however you prefer (public fields or getters on private fields).

### Example target model

```java
import java.util.*;

public class AppData {
    private String name;
    private Integer port = 8080; // default kept when key is missing
    private Optional<String> user = Optional.empty();
    private Mode mode = Mode.DEV;
    private Db db = new Db();
    private List<Tag> tags = new ArrayList<>();
    private Map<String, PositiveInt> limits = new LinkedHashMap<>();

    public String getName() { return name; }
    public Integer getPort() { return port; }
    public Optional<String> getUser() { return user; }
    public Mode getMode() { return mode; }
    public Db getDb() { return db; }
    public List<Tag> getTags() { return tags; }
    public Map<String, PositiveInt> getLimits() { return limits; }

    public enum Mode { DEV, PROD }

    public static class Db {
        public String host = "localhost";
        public Optional<String> password = Optional.empty();
    }

    public static class Tag {
        public String value;
    }

    public static class PositiveInt {
        public final Integer value;
        public PositiveInt(Integer value) {
            if (value == null || value <= 0) throw new IllegalArgumentException("must be > 0");
            this.value = value;
        }
    }
}
```

## Quick usage

```java
import org.msuo.data2java.*;

AppData luaData = new LuaDeserializer().deserialize(
    "return { name = 'svc', port = 9090, mode = 'PROD' }",
    AppData.class
);

AppData groovyData = new GroovyDeserializer().deserialize(
    "return [name: 'svc', port: 9090, mode: 'PROD']",
    AppData.class
);

AppData tomlData = new TomlDeserializer().deserialize(
    "name = 'svc'\nport = 9090\nmode = 'PROD'",
    AppData.class
);

AppData jsonData = new JsonDeserializer().deserialize(
    "{\"name\":\"svc\",\"port\":9090,\"mode\":\"PROD\"}",
    AppData.class
);

AppData xmlData = new XmlDeserializer().deserialize(
    "<data><name>svc</name><port>9090</port><mode>PROD</mode></data>",
    AppData.class
);
```

## Nested generics example

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

GenericData data = new JsonDeserializer().deserialize(
    "{\"foo\":{\"value\":[{\"payload\":\"a\"}]},\"values\":{\"k1\":[\"x\",\"y\"]}}",
    GenericData.class
);
```

## Class reference example

```java
interface Service {}
class ServiceImpl implements Service {}
class ServiceData {
    public Class<Service> impl;
}

ServiceData data = new JsonDeserializer().deserialize(
    "{\"impl\":\"" + ServiceImpl.class.getName() + "\"}",
    ServiceData.class
);

assertEquals(ServiceImpl.class, data.impl);
```

Any class assignable to `T` is accepted for `Class<T>`.

## Validation and errors

Object mapping and validation are done in one pass. Unknown input fields (including nested ones) are treated as mapping errors and are included in the same aggregated exception. Field errors are collected, then one `DataDeserializationException` is thrown with all errors.
The exception message also includes a tree view of failing paths.

```java
try {
    new JsonDeserializer().deserialize("{\"port\":0}", AppData.class);
} catch (DataDeserializationException ex) {
    ex.forEachError((segments, error) ->
        System.out.println(segments + " -> " + error.getErrorType().getClass().getSimpleName())
    );
    DataDeserializationException.PathNode root = ex.getErrorPathTree();
    System.out.println(ex.getMessage());
}
```

See [errors.md](errors.md) for all error variants.

Parse/eval failures are separate and throw `DataSourceException`.

```java
try {
    new TomlDeserializer().deserialize("name = ", AppData.class);
} catch (DataSourceException ex) {
    System.out.println(ex.phase());   // parse or evaluate
    System.out.println(ex.format());  // TOML / JSON / XML / Lua / Groovy
}
```

## Format-specific behavior

Format-specific semantics are documented in each module README:

- [modules/lua2java](modules/lua2java/README.md)
- [modules/groovy2java](modules/groovy2java/README.md)
- [modules/toml2java](modules/toml2java/README.md)
- [modules/json2java](modules/json2java/README.md)
- [modules/xml2java](modules/xml2java/README.md)

## Build and test

```bash
./gradlew clean test
```

Run selected modules:

```bash
./gradlew :lua2java:test :groovy2java:test :toml2java:test :json2java:test :xml2java:test
```

## Notes

- Supported Java versions: Java 11 and above for all modules.
- LLM assistance was used during development of this codebase and its documentation.
