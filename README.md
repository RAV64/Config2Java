# Config2Java

Config2Java maps configuration data into validated **null-safe** Java objects.

This repository is a monorepo with one core module and format-specific modules. You can depend on exactly the formats you need.

## Modules

- [modules/deserializer](modules/deserializer/README.md): core object-mapping/validation engine
- [modules/lua2java](modules/lua2java/README.md): Lua deserializer
- [modules/groovy2java](modules/groovy2java/README.md): Groovy deserializer
- [modules/toml2java](modules/toml2java/README.md): TOML deserializer
- [modules/json2java](modules/json2java/README.md): JSON deserializer
- [modules/xml2java](modules/xml2java/README.md): XML deserializer
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
- Enums are parsed from string names.
- Expose values however you prefer (public fields or getters on private fields).

### Example target model

```java
import java.util.*;

public class AppConfig {
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
import org.msuo.config2java.*;

AppConfig luaCfg = new LuaDeserializer().deserialize(
    "return { name = 'svc', port = 9090, mode = 'PROD' }",
    AppConfig.class
);

AppConfig groovyCfg = new GroovyDeserializer().deserialize(
    "return [name: 'svc', port: 9090, mode: 'PROD']",
    AppConfig.class
);

AppConfig tomlCfg = new TomlDeserializer().deserialize(
    "name = 'svc'\nport = 9090\nmode = 'PROD'",
    AppConfig.class
);

AppConfig jsonCfg = new JsonDeserializer().deserialize(
    "{\"name\":\"svc\",\"port\":9090,\"mode\":\"PROD\"}",
    AppConfig.class
);

AppConfig xmlCfg = new XmlDeserializer().deserialize(
    "<config><name>svc</name><port>9090</port><mode>PROD</mode></config>",
    AppConfig.class
);
```

## Validation and errors

Object mapping and validation are done in one pass. Field errors are collected, then one `ConfigDeserializationException` is thrown with all errors.

```java
try {
    new JsonDeserializer().deserialize("{\"port\":0}", AppConfig.class);
} catch (ConfigDeserializationException ex) {
    ex.getErrors().forEach(e ->
        System.out.println(e.getPath() + " -> " + e.getMessage())
    );
}
```

See [errors.md](errors.md) for all error variants.

Parse/eval failures are separate and throw `ConfigSourceException`.

```java
try {
    new TomlDeserializer().deserialize("name = ", AppConfig.class);
} catch (ConfigSourceException ex) {
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
