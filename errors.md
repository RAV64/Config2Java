# Error Reference

Config2Java binding/validation errors are format-agnostic and are reported as `ConfigDeserializationException`.

## Error structure

Each error contains:

- `path`: location such as `$.db.port`, `$.tags[1]`, `$.limits{foo}`
- `message`: human-readable error message

## Handling errors

```java
import org.msuo.config2java.ConfigDeserializationException;
import org.msuo.config2java.Deserializer;

try {
    Deserializer d = ...;
    d.deserialize(source, MyCfg.class);
} catch (ConfigDeserializationException ex) {
    for (ConfigDeserializationException.ConfigError e : ex.getErrors()) {
        System.out.println(e.getPath() + " -> " + e.getMessage());
    }
}
```

## Common categories

- missing required fields
- wrong scalar/table shape
- invalid value-object constructor type
- constructor rejection (domain validation)
- enum parse failures
- unsupported type declarations (for nested generics etc)
- nested-object instantiation/field-set reflection failures

Parse/eval failures in a language parser (invalid Lua/Groovy/TOML/JSON/XML input) are raised by that language module before binder errors are produced.
