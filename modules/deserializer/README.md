# deserializer

`deserializer` is the format-agnostic core of Config2Java.

Language modules convert native parse trees to `ConfigValue`/`ConfigTable`, then this module maps those values into Java objects and validates them.

## Responsibilities

- map config trees to Java objects
- apply defaults and optional semantics
- parse enums and generic containers (`Optional`, `List`, `Set`, `Map`)
- perform constructor-based leaf validation
- aggregate path-based errors into one exception

## Public API

`org.msuo.config2java.Deserializer`:

- `deserialize(String source, Class<T> configClass)`
- default overloads for `Path`/`File` with charset

## Mapping model

- Object field mapping is iterative and single-pass.
- Missing key and explicit nil are handled differently.
- Missing required fields fail unless a default already exists.
- `Optional<T>` maps missing/nil to `Optional.empty()`.

## For language implementers

A language module typically:

1. Parses source text into native objects.
2. Exposes them through `ConfigValue` + `ConfigTable` adapters.
3. Extends `TreeDeserializer` and returns the root `ConfigValue`.

The core mapper then handles the rest.

## Error model

See [../../errors.md](../../errors.md) for all error types, examples, and fixes.
