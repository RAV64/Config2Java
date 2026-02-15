# groovy2java

Groovy implementation for Config2Java.

## Depends on

- [../deserializer](../deserializer/README.md)
- `org.apache.groovy:groovy`

## Usage

```java
import org.msuo.config2java.GroovyDeserializer;

String groovy = """
return [
  app: [host: 'localhost', port: 8080],
  mode: 'DEV'
]
""";

MyCfg cfg = new GroovyDeserializer().deserialize(groovy, MyCfg.class);
```

Groovy implicit last-expression return also works.

## Environment/global injection

```java
import org.msuo.config2java.GroovyDeserializer;

GroovyDeserializer d = GroovyDeserializer.builder()
    .env("APP_ENV", "prod")
    .global("defaultName", "worker-default")
    .build();

String groovy = """
def c = [mode: 'DEV', name: defaultName]
if (ENV.APP_ENV == 'prod') c.mode = 'PROD'
return c
""";

MyCfg cfg = d.deserialize(groovy, MyCfg.class);
```

## Optional null behavior

Explicit `null` is treated as a provided nil value.

```java
class Cfg {
    public java.util.Optional<String> name = java.util.Optional.of("default");
}

Cfg cfg = new GroovyDeserializer().deserialize("return [name: null]", Cfg.class);
// cfg.name == Optional.empty()
```

Shared errors: [../../errors.md](../../errors.md).
