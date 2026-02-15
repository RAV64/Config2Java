# json2java

JSON implementation for Config2Java.

## Depends on

- [../deserializer](../deserializer/README.md)
- `com.fasterxml.jackson.core:jackson-databind`

## Usage

```java
import org.msuo.config2java.JsonDeserializer;

String json = """
{
  "app": { "host": "localhost", "port": 8080 },
  "mode": "DEV"
}
""";

MyCfg cfg = new JsonDeserializer().deserialize(json, MyCfg.class);
```

Shared errors: [../../errors.md](../../errors.md).
