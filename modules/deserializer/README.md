# deserializer

`deserializer` is the format-agnostic core of Config2Java.

Language modules convert native parse trees to `ConfigValue`/`ConfigTable`, then this module maps those values into Java objects and validates them.

## Responsibilities

- map config trees to Java objects
- apply defaults and optional semantics
- parse enums and generic containers (`Optional`, `List`, `Set`, `Map`)
- perform constructor-based leaf validation
- aggregate path-based errors into one exception

## Dependency

Gradle dependency:

```gradle
implementation "org.msuo:deserializer:<version>"
```

## Mapping model

- Object field mapping is iterative and single-pass.
- Missing key and explicit nil are handled differently.
- Missing required fields fail unless a default already exists.
- `Optional<T>` maps missing/nil to `Optional.empty()`.

## For language implementers

`Deserializer` defines the required API:

- implement `deserialize(String source, Class<T> configClass)`
- reuse default file overloads from the interface (`Path` / `File`)

Script formats can implement `ScriptDeserializer`, which adds:

- `deserialize(String, Class<T>, Map<String, String> env, Map<String, ?> globals)`
- default no-injection `deserialize(String, Class<T>)`

`AbstractScriptDeserializer` provides shared env/global normalization and delegates format-specific parsing through `parse(...)`.

A language module typically:

1. Parses source text into native objects.
2. Exposes them through `ConfigValue` + `ConfigTable` adapters.
3. Calls `ObjectMapper.deserialize(...)` with the root `ConfigValue`.

The core mapper then handles the rest.

## Error model

`ConfigDeserializationException` exposes:

- `getErrors()` for flat structured error entries
- `forEachError((pathSegments, error) -> ...)` for typed iteration without message parsing
- each error entry includes `getErrorType()`, `getPathSegments()`, and `getMessage()`
- `getErrorPathTree()` for structured nested path traversal
- `getMessage()` for rendered tree output

See [../../errors.md](../../errors.md) for all error types, examples, and fixes.
