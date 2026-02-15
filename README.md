# Config2Java

Config2Java maps configuration data into validated Java objects.

This repository is a monorepo with one core module and format-specific modules.

## Repository layout

- [modules/deserializer](modules/deserializer/README.md): core binder, validation, shared error model
- [modules/lua2java](modules/lua2java/README.md): Lua support
- [modules/groovy2java](modules/groovy2java/README.md): Groovy support
- [modules/toml2java](modules/toml2java/README.md): TOML support
- [modules/json2java](modules/json2java/README.md): JSON support
- [modules/xml2java](modules/xml2java/README.md): XML support
- [errors.md](errors.md): shared error reference

## Dependency model

Each language module depends on `deserializer` only.

You can depend on one or many modules. For example, using `lua2java` does not pull `groovy2java`, `toml2java`, `json2java`, or `xml2java`.

## Shared behavior across all formats

- nested object binding
- constructor-based value-object validation
- `Optional<T>` support
- default field values preserved when config key is missing
- list/set/map support
- enum parsing from string names
- aggregated path-based errors via `ConfigDeserializationException`

## Quick examples

```java
import org.msuo.config2java.LuaDeserializer;
import org.msuo.config2java.GroovyDeserializer;
import org.msuo.config2java.TomlDeserializer;
import org.msuo.config2java.JsonDeserializer;
import org.msuo.config2java.XmlDeserializer;

MyCfg lua = new LuaDeserializer().deserialize("return { port = 8080 }", MyCfg.class);
MyCfg groovy = new GroovyDeserializer().deserialize("return [port: 8080]", MyCfg.class);
MyCfg toml = new TomlDeserializer().deserialize("port = 8080", MyCfg.class);
MyCfg json = new JsonDeserializer().deserialize("{\"port\":8080}", MyCfg.class);
MyCfg xml = new XmlDeserializer().deserialize("<config><port>8080</port></config>", MyCfg.class);
```

`Deserializer` also supports `Path`/`File` overloads.

## Build and test

```bash
./gradlew test
```

Run one module:

```bash
./gradlew :lua2java:test
```
