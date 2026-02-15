# toml2java

TOML implementation for Config2Java.

## Depends on

- [../deserializer](../deserializer/README.md)
- `org.tomlj:tomlj`

## Usage

```java
import org.msuo.config2java.TomlDeserializer;

String toml = """
[app]
host = "localhost"
port = 8080
mode = "DEV"
""";

MyCfg cfg = new TomlDeserializer().deserialize(toml, MyCfg.class);
```

## Notes

- TOML parse errors fail before binding.
- TOML has no explicit nil literal; missing means missing.

Shared errors: [../../errors.md](../../errors.md).
