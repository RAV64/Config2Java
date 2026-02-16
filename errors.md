# Error Reference

This document lists all object-mapping/validation errors emitted by the core deserializer (`modules/deserializer/src/main/java/org/msuo/config2java/Errors.java`).

These are emitted inside `ConfigDeserializationException`.

It does not include language parser/evaluator failures (for example invalid Lua/Groovy/TOML/JSON/XML syntax), which throw `ConfigSourceException` before object mapping starts.

## Error shape

Each error entry has:

- `pathSegments`: structured path segments (for example `["db", "port"]`, `["tags", "[1]"]`, `["limits", "{foo}"]`)
- `errorType`: typed variant (`ConfigErrorType`, for example `ConfigErrorTypes.MissingRequiredField`)
- `message`: message from `ConfigErrorType.message()`

`ConfigDeserializationException` message includes a hierarchical path tree with each error shown at the node where it occurred.

Example:

```java
try {
    deserializer.deserialize(source, MyCfg.class);
} catch (ConfigDeserializationException ex) {
    ex.forEachError((segments, error) -> {
        System.out.println(segments);
        System.out.println(error.getErrorType().getClass().getSimpleName());
        System.out.println(error.getMessage());
    });
}
```

## Complex nested example

This example shows how aggregated errors and the structured path tree behave for nested objects and optionals.

```java
import java.util.Optional;

class AppCfg {
    public Db db;
    public Optional<Service> service = Optional.empty();
    public PositiveInteger retries;

    static class Db {
        public NonEmptyString host;
        public PositiveInteger port;
    }

    static class Service {
        public NonEmptyString name;
    }

    static class NonEmptyString {
        public final String value;
        public NonEmptyString(String v) {
            if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException("must be non-empty");
            this.value = v;
        }
    }

    static class PositiveInteger {
        public final Integer value;
        public PositiveInteger(Integer v) {
            if (v == null || v <= 0) throw new IllegalArgumentException("must be > 0");
            this.value = v;
        }
    }
}
```

Failing config (JSON for brevity):

```json
{
  "db": {
    "host": "",
    "port": 0
  },
  "service": {
    "name": ""
  },
  "retries": 0
}
```

Handling errors via API:

```java
try {
    new JsonDeserializer().deserialize(source, AppCfg.class);
} catch (ConfigDeserializationException ex) {
    for (ConfigDeserializationException.ConfigError e : ex.getErrors()) {
        System.out.println(
            e.getPathSegments() + " -> " + e.getErrorType().getClass().getSimpleName()
        );
    }

    ConfigDeserializationException.PathNode root = ex.getErrorPathTree();
    // root.getSegment() == "$"
    // root.getChildren() contains "db", "service", "retries"
}
```

Expected error paths:

- `$.db.host`
- `$.db.port`
- `$.service.name`
- `$.retries`

`ex.getMessage()` output:

```text
$
├─ db
|  ├─ host -> Value [] rejected by NonEmptyString: must be non-empty
|  └─ port -> Value [0] rejected by PositiveInteger: must be > 0
├─ service
|  └─ name -> Value [] rejected by NonEmptyString: must be non-empty
└─ retries -> Value [0] rejected by PositiveInteger: must be > 0
```

## Mapping error variants

### 1) `UnsupportedType`
Message:
`Unsupported Type: <type>`

Real trigger:
A field type resolves to a `Type` that is neither `Class<?>` nor `ParameterizedType` in the mapper.

How to fix:
Use supported field declarations (`Class`, `Enum`, `Optional<T>`, `Collection<T>`, `Map<K,V>`).

### 2) `UnsupportedParameterized`
Message:
`Unsupported parameterized type: <type>`

Real trigger:
A parameterized type has a class raw type, but the raw type is not `Optional`, `Collection`/`List`/`Set`, or `Map`.

How to fix:
Use supported generics, or wrap unsupported generic structures in concrete classes.

### 3) `UnsupportedParameterizedRaw`
Message:
`Unsupported parameterized raw type: <raw>`

Real trigger:
Parameterized raw type is not a `Class<?>`.

How to fix:
Use normal class-based generic declarations.

### 4) `PrimitiveNotSupported`
Message:
`Primitive field types are not supported: <primitive>`

Real trigger:
A target field is primitive (`int`, `boolean`, etc.).

How to fix:
Use boxed types (`Integer`, `Boolean`, etc.).

### 5) `EnumExpectedString`
Message:
`Enum expects string name, got: <type>`

Real trigger:
Enum field receives non-string scalar or non-scalar value.

How to fix:
Provide enum as a string value.

### 6) `EnumUnknown`
Message:
`Unknown enum value '<value>' for <EnumClass>. Valid values: [A, B, ...]`

Real trigger:
String value does not match any enum constant name.

How to fix:
Use a valid enum constant name exactly.

### 7) `ExpectedScalar`
Message:
`Expected primitive (string/number/bool), got: <type>`

Real trigger:
Leaf/value-object target receives table/array/object instead of scalar.

How to fix:
Provide scalar input or change Java field type to object/collection.

### 8) `MapExpected`
Message:
`Expected table for Map, got: <type>`

Real trigger:
`Map<K,V>` field receives non-table input.

How to fix:
Provide object/table-like input.

### 9) `CollectionExpected`
Message:
`Expected table/array for <CollectionType>, got: <type>`

Real trigger:
`Collection/List/Set` field receives non-array/table input.

How to fix:
Provide list/array/table-like input.

### 10) `OptionalInnerMustBeConcrete`
Message:
`Optional inner type must be a concrete class (no nested generics). Got: <type>`

Real trigger:
`Optional<T>` inner type is not a concrete class.

How to fix:
Use `Optional<ConcreteType>`.

### 11) `CollectionElementMustBeConcrete`
Message:
`Collection element type must be a concrete class (no nested generics). Got: <type>`

Real trigger:
Collection element type is not a concrete class.

How to fix:
Use concrete element types.

### 12) `MapKeyMustBeConcrete`
Message:
`Map key type must be a concrete class (no nested generics). Got: <type>`

Real trigger:
Map key type is not a concrete class.

How to fix:
Use concrete key types.

### 13) `MapValueMustBeConcrete`
Message:
`Map value type must be a concrete class (no nested generics). Got: <type>`

Real trigger:
Map value type is not a concrete class.

How to fix:
Use concrete value types.

### 14) `MissingRequiredField`
Message:
`Missing required field (no default value).`

Real trigger:
Key is missing and field has no default and no optional/missing adapter fallback.

How to fix:
Provide key, set a default, or change field to `Optional<T>`.

### 15) `NoOneArgCtor`
Message:
`No 1-arg constructor on <Type> accepting <ScalarType>`

Real trigger:
Leaf value mapping needs value-object construction, but constructor signature does not match scalar type.

How to fix:
Add matching one-arg constructor or change input scalar type.

### 16) `CtorRejected`
Message:
`Value [<value>] rejected by <Type>: <reason>`

Real trigger:
One-arg constructor exists and throws (typically validation failure).

How to fix:
Fix input value or constructor validation logic.

Example:
`Value [-1] rejected by PositiveInteger: must be > 0`

### 17) `CtorCallFailed`
Message:
`Failed calling constructor for <Type>: <reason>`

Real trigger:
One-arg constructor invocation fails reflectively for non-validation reasons.

How to fix:
Check constructor accessibility/reflective constraints and type assumptions.

### 18) `NoNoArgCtor`
Message:
`No no-arg constructor for nested object type: <Type>`

Real trigger:
Object mapping requires a no-arg constructor and none exists.

How to fix:
Add a no-arg constructor or change model shape.

### 19) `CtorFailed`
Message:
`Constructor failed for <Type>: <reason>`

Real trigger:
No-arg constructor exists but throws while instantiating nested object.

How to fix:
Avoid throwing from construction path used by object mapping.

### 20) `InstantiateFailed`
Message:
`Failed to instantiate <Type>: <reason>`

Real trigger:
Reflective no-arg instantiation fails (for example abstract type or reflection failure).

How to fix:
Use concrete instantiable field types.

### 21) `FieldSetAccess`
Message:
`Failed to set field (access): <reason>`

Real trigger:
Reflection cannot assign field due to access/security restriction at runtime.

How to fix:
Allow reflective field access in runtime environment.

### 22) `FieldSetTypeMismatch`
Message:
`Failed to set field (type mismatch): <reason>`

Real trigger:
Bound value type is incompatible with field type.

How to fix:
Align field declaration with actual bound value type.

### 23) `FieldAccessSetup`
Message:
`Failed to access field reflectively: <reason>`

Real trigger:
Runtime blocks reflective access setup (`setAccessible(true)`), for example module access restrictions.

How to fix:
Open module/package for reflection, or use a runtime that allows reflective access for your config model types.

### 24) `FieldReadAccess`
Message:
`Failed to read field default value: <reason>`

Real trigger:
Deserializer cannot read the current field value when deciding whether a default already exists.

How to fix:
Allow reflective read access for model fields in the runtime environment.

## Source parse/eval failures

`ConfigSourceException` is thrown before object mapping for syntax/eval issues.

- `format()`: `Lua`, `Groovy`, `TOML`, `JSON`, `XML`
- `phase()`: `evaluate` (script formats) or `parse` (data formats)
- message format: `Failed to <phase> <format>: <details>`

## Practical debugging flow

1. Inspect `pathSegments` to locate offending field.
2. Inspect `errorType` to identify exact variant.
3. If needed, inspect `getErrorPathTree()` to traverse grouped nested failures programmatically.
4. Apply the fix to either config input or Java model.
5. Re-run and handle remaining aggregated errors.
