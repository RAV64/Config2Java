# deserializer

`deserializer` is the format-agnostic core of Config2Java.

## What this module does

- binds config trees into Java objects
- validates leaf/value objects through 1-arg constructors
- supports defaults, `Optional<T>`, collections, maps, enums
- aggregates all binding errors with stable paths

## Internal contract used by language modules

Language modules only need to provide native `ConfigValue`/`ConfigTable` adapters.

The binder then performs deserialization and validation in a single traversal path.

## Public API

`org.msuo.config2java.Deserializer`:

- `deserialize(String source, Class<T> targetType)`
- default overloads for `Path`/`File` and charset

For error types/messages and path format, see [../../errors.md](../../errors.md).
