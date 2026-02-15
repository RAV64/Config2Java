# lua2java

Lua implementation for Config2Java.

## Depends on

- [../deserializer](../deserializer/README.md)
- `org.luaj:luaj-jse`

## Usage

```java
import org.msuo.config2java.LuaDeserializer;

String lua = """
return {
  app = { host = 'localhost', port = 8080 },
  mode = 'DEV'
}
""";

MyCfg cfg = new LuaDeserializer().deserialize(lua, MyCfg.class);
```

## Environment/global injection

```java
import org.msuo.config2java.LuaDeserializer;

LuaDeserializer d = LuaDeserializer.builder()
    .env("APP_ENV", "prod")
    .global("defaultName", "worker-default")
    .build();

String lua = """
local c = { mode = 'DEV', name = defaultName }
if os.getenv('APP_ENV') == 'prod' then c.mode = 'PROD' end
return c
""";

MyCfg cfg = d.deserialize(lua, MyCfg.class);
```

## Notes

- Lua config should return the root value (commonly `return { ... }`).
- Missing keys keep Java defaults.

Shared errors: [../../errors.md](../../errors.md).
