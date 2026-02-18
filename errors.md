# Error Reference

This document lists all object-mapping/validation errors emitted by the core deserializer (`modules/deserializer/src/main/java/org/msuo/data2java/Errors.java`).

These are emitted inside `DataDeserializationException`.

It does not include language parser/evaluator failures (for example invalid Lua/Groovy/TOML/JSON/XML syntax), which throw `DataSourceException` before object mapping starts.

## Error shape

Each error entry has:

- `pathSegments`: structured path segments (for example `["db", "port"]`, `["tags", "[1]"]`, `["limits", "{foo}"]`)
- `errorType`: typed variant (`DataErrorType`, for example `DataErrorTypes.MissingRequiredField`)
- `message`: message from `DataErrorType.message()`

`DataDeserializationException` message includes a hierarchical path tree with each error shown at the node where it occurred.

Example:

```java
try {
    new JsonDeserializer().deserialize(source, MyDataModel.class);
} catch (DataDeserializationException ex) {
    ex.forEachError((segments, error) -> {
        System.out.println(segments);
        System.out.println(error.getErrorType().getClass().getSimpleName());
        System.out.println(error.getMessage());
    });
}
```

## Complex nested example

This example mirrors the error-output showcase test and demonstrates multiple error kinds across nested objects.

```java
class ShowcaseData {
    public Db db;
    public Service service;
    public java.util.Map<NonEmptyString, PositiveInteger> limits;
    public NoNoArgNested bad;
    public Feature feature;
    public Class<Worker> workerImpl;
    public PositiveDouble ratio;

    static class Db {
        public NonEmptyString host;
        public PositiveInteger port;
    }

    static class Service {
        public Mode mode;
        public Auth auth;
    }

    enum Mode {
        DEV,
        PROD
    }

    static class Auth {
        public NonEmptyString token;
        public PositiveInteger ttl;
    }

    static class Feature {
        public NonEmptyString name;
    }

    interface Worker {}

    static class WorkerImpl implements Worker {}

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

    static class PositiveDouble {
        public final Double value;
        public PositiveDouble(Double v) {
            if (v == null || v <= 0.0) throw new IllegalArgumentException("must be > 0");
            this.value = v;
        }
    }

    static class NoNoArgNested {
        public NonEmptyString x;
        public NoNoArgNested(NonEmptyString x) {
            this.x = x;
        }
    }
}
```

Failing data (JSON):

```json
{
  "db": {
    "host": "",
    "port": 0
  },
  "service": {
    "mode": "NOPE",
    "extra": true,
    "auth": {
      "token": "",
      "ttl": 0
    }
  },
  "limits": {
    "ok": 1,
    "bad": 0
  },
  "bad": {
    "x": "ok"
  },
  "feature": {},
  "workerImpl": "java.lang.String",
  "ratio": 1,
  "unexpectedTop": "x"
}
```

Handling errors via API:

```java
try {
    new JsonDeserializer().deserialize(source, ShowcaseData.class);
} catch (DataDeserializationException ex) {
    ex.forEachError((segments, error) ->
        System.out.println(segments + " -> " + error.getErrorType().getClass().getSimpleName())
    );
    System.out.println(ex.getMessage());
}
```

Expected path segments and error kinds include:

- `["db", "host"]` -> `CtorRejected`
- `["db", "port"]` -> `CtorRejected`
- `["service", "mode"]` -> `EnumUnknown`
- `["service", "extra"]` -> `UnknownField`
- `["service", "auth", "token"]` -> `CtorRejected`
- `["service", "auth", "ttl"]` -> `CtorRejected`
- `["limits", "[bad]"]` -> `CtorRejected`
- `["bad"]` -> `NoNoArgCtor`
- `["feature", "name"]` -> `MissingRequiredField`
- `["workerImpl"]` -> `ClassRefNotAssignable`
- `["ratio"]` -> `NoOneArgCtor`
- `["unexpectedTop"]` -> `UnknownField`

`ex.getMessage()` output:

```text
$
├─ db
|  ├─ host -> Value [] rejected by NonEmptyString: must be non-empty
|  └─ port -> Value [0] rejected by PositiveInteger: must be > 0
├─ service
|  ├─ mode -> Unknown enum value 'NOPE' for ShowcaseData$Mode. Valid values: [DEV, PROD]
|  ├─ extra -> Unknown field 'extra' for ShowcaseData
|  └─ auth
|     ├─ token -> Value [] rejected by NonEmptyString: must be non-empty
|     └─ ttl -> Value [0] rejected by PositiveInteger: must be > 0
├─ limits
|  └─ [bad] -> Value [0] rejected by PositiveInteger: must be > 0
├─ bad -> No no-arg constructor for nested object type: ShowcaseData$NoNoArgNested
├─ feature
|  └─ name -> Missing required field (no default value).
├─ workerImpl -> Class java.lang.String is not assignable to ShowcaseData$Worker
├─ ratio -> No 1-arg constructor on ShowcaseData$PositiveDouble accepting java.lang.Integer
└─ unexpectedTop -> Unknown field 'unexpectedTop' for ShowcaseData
```

## Mapping error variants

### 1) `UnsupportedType`
Message:
`Unsupported Type: <type>`

Real trigger:
A field type resolves to a `Type` that is neither `Class<?>` nor `ParameterizedType` in the mapper.

How to fix:
Use supported field declarations (`Class<?>` and parameterized types with concrete raw classes, including nested generics).

### 2) `UnresolvedTypeVariable`
Message:
`Unresolved generic type variable: <TypeVariable>`

Real trigger:
A field resolves to a generic type variable that cannot be resolved to a concrete type in the target object graph.

How to fix:
Use concrete generic types in deserialization targets.

### 3) `WildcardTypeNotSupported`
Message:
`Wildcard generic types are not supported here: <WildcardType>`

Real trigger:
A wildcard type appears in a place where mapping requires a concrete resolved type.

How to fix:
Use concrete generic arguments for deserialization target fields.

### 4) `UnsupportedParameterizedRaw`
Message:
`Unsupported parameterized raw type: <raw>`

Real trigger:
Parameterized raw type is not a `Class<?>`.

How to fix:
Use normal class-based generic declarations.

### 5) `PrimitiveNotSupported`
Message:
`Primitive field types are not supported: <primitive>`

Real trigger:
A target field is primitive (`int`, `boolean`, etc.).

How to fix:
Use boxed types (`Integer`, `Boolean`, etc.).

### 6) `EnumExpectedString`
Message:
`Enum expects string name, got: <type>`

Real trigger:
Enum field receives non-string scalar or non-scalar value.

How to fix:
Provide enum as a string value.

### 7) `EnumUnknown`
Message:
`Unknown enum value '<value>' for <EnumClass>. Valid values: [A, B, ...]`

Real trigger:
String value does not match any enum constant name.

How to fix:
Use a valid enum constant name exactly.

### 8) `ExpectedScalar`
Message:
`Expected primitive (string/number/bool), got: <type>`

Real trigger:
Leaf/value-object target receives table/array/object instead of scalar.

How to fix:
Provide scalar input or change Java field type to object/collection.

### 9) `MapExpected`
Message:
`Expected table for Map, got: <type>`

Real trigger:
`Map<K,V>` field receives non-table input.

How to fix:
Provide object/table-like input.

### 10) `CollectionExpected`
Message:
`Expected table/array for <CollectionType>, got: <type>`

Real trigger:
`Collection/List/Set` field receives non-array/table input.

How to fix:
Provide list/array/table-like input.

### 11) `MissingRequiredField`
Message:
`Missing required field (no default value).`

Real trigger:
Key is missing and field has no default and no optional/missing adapter fallback.

How to fix:
Provide key, set a default, or change field to `Optional<T>`.

### 12) `UnknownField`
Message:
`Unknown field '<field>' for <Type>`

Real trigger:
Input contains a key that does not map to any field on the current target object type.

How to fix:
Remove the unexpected key from input data or add a matching field to the Java model.

XML note:
For structured XML elements with mixed text and child elements, text content may appear as `text`; if the target type has no `text` field, this is also reported as `UnknownField`.

### 13) `NoOneArgCtor`
Message:
`No 1-arg constructor on <Type> accepting <ScalarType>`

Real trigger:
Leaf value mapping needs value-object construction, but constructor signature does not match scalar type.

How to fix:
Add matching one-arg constructor or change input scalar type.

### 14) `CtorRejected`
Message:
`Value [<value>] rejected by <Type>: <reason>`

Real trigger:
One-arg constructor exists and throws (typically validation failure).

How to fix:
Fix input value or constructor validation logic.

Example:
`Value [-1] rejected by PositiveInteger: must be > 0`

### 15) `CtorCallFailed`
Message:
`Failed calling constructor for <Type>: <reason>`

Real trigger:
One-arg constructor invocation fails reflectively for non-validation reasons.

How to fix:
Check constructor accessibility/reflective constraints and type assumptions.

### 16) `NoNoArgCtor`
Message:
`No no-arg constructor for nested object type: <Type>`

Real trigger:
Object mapping requires a no-arg constructor and none exists.

How to fix:
Add a no-arg constructor or change model shape.

### 17) `CtorFailed`
Message:
`Constructor failed for <Type>: <reason>`

Real trigger:
No-arg constructor exists but throws while instantiating nested object.

How to fix:
Avoid throwing from construction path used by object mapping.

### 18) `InstantiateFailed`
Message:
`Failed to instantiate <Type>: <reason>`

Real trigger:
Reflective no-arg instantiation fails (for example abstract type or reflection failure).

How to fix:
Use concrete instantiable field types.

### 19) `FieldSetAccess`
Message:
`Failed to set field (access): <reason>`

Real trigger:
Reflection cannot assign field due to access/security restriction at runtime.

How to fix:
Allow reflective field access in runtime environment.

### 20) `FieldSetTypeMismatch`
Message:
`Failed to set field (type mismatch): <reason>`

Real trigger:
Bound value type is incompatible with field type.

How to fix:
Align field declaration with actual bound value type.

### 21) `FieldAccessSetup`
Message:
`Failed to access field reflectively: <reason>`

Real trigger:
Runtime blocks reflective access setup (`setAccessible(true)`), for example module access restrictions.

How to fix:
Open module/package for reflection, or use a runtime that allows reflective access for your data model types.

### 22) `ClassRefExpectedString`
Message:
`Class reference expects string class name, got: <type>`

Real trigger:
A `Class<T>` field receives a non-string value.

How to fix:
Provide the class as a fully qualified string name.

### 23) `ClassRefNotFound`
Message:
`Class not found: <className>`

Real trigger:
A `Class<T>` field receives a string class name that cannot be loaded.

How to fix:
Use a valid class name available on the application classpath.

### 24) `ClassRefNotAssignable`
Message:
`Class <ActualType> is not assignable to <ExpectedType>`

Real trigger:
A `Class<T>` field resolves to a class that is not assignable to `T`.

How to fix:
Use a class name that resolves to `T` or a subtype of `T`.

### 25) `FieldReadAccess`
Message:
`Failed to read field default value: <reason>`

Real trigger:
Deserializer cannot read the current field value when deciding whether a default already exists.

How to fix:
Allow reflective read access for model fields in the runtime environment.

## Source parse/eval failures

`DataSourceException` is thrown before object mapping for syntax/eval issues.

- `format()`: `Lua`, `Groovy`, `TOML`, `JSON`, `XML`
- `phase()`: `evaluate` (script formats) or `parse` (data formats)
- message format: `Failed to <phase> <format>: <details>`

## Practical debugging flow

1. Inspect `pathSegments` to locate offending field.
2. Inspect `errorType` to identify exact variant.
3. If needed, inspect `getErrorPathTree()` to traverse grouped nested failures programmatically.
4. Apply the fix to either input data or Java model.
5. Re-run and handle remaining aggregated errors.
