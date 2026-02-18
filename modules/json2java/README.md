# json2java

JSON to Java deserializer for Data2Java.

Depends on:
- [../deserializer](../deserializer/README.md)
- `com.fasterxml.jackson.core:jackson-databind`

```gradle
implementation "org.msuo:json2java:<version>"
```

## Leaf values

```java
class DataModel {
    public String name;
    public Integer port;
}

String json = "{\"name\":\"svc\",\"port\":8080}";
DataModel data = new JsonDeserializer().deserialize(json, DataModel.class);

assertEquals("svc", data.name);
assertEquals(Integer.valueOf(8080), data.port);
```

## Missing keys keep defaults

```java
class DataModel {
    public String name = "default-name";
    public Integer port;
}

DataModel data = new JsonDeserializer().deserialize("{\"port\":8080}", DataModel.class);
assertEquals("default-name", data.name);
```

## Nested objects

```java
class DataModel {
    public Db db;
    static class Db {
        public String host;
        public Integer port;
    }
}

String json = "{\"db\":{\"host\":\"db\",\"port\":15432}}";
DataModel data = new JsonDeserializer().deserialize(json, DataModel.class);

assertEquals("db", data.db.host);
assertEquals(Integer.valueOf(15432), data.db.port);
```

## Optionals

```java
import java.util.Optional;

class DataModel {
    public Optional<String> user = Optional.of("default-user");
}

DataModel present = new JsonDeserializer().deserialize("{\"user\":\"alice\"}", DataModel.class);
DataModel missing = new JsonDeserializer().deserialize("{}", DataModel.class);

assertEquals(Optional.of("alice"), present.user);
assertEquals(Optional.of("default-user"), missing.user);
```

## Collections and maps

```java
import java.util.List;
import java.util.Map;

class DataModel {
    public List<String> tags;
    public Map<String, Integer> limits;
}

String json = "{\"tags\":[\"a\",\"b\"],\"limits\":{\"api\":10}}";
DataModel data = new JsonDeserializer().deserialize(json, DataModel.class);

assertEquals(List.of("a", "b"), data.tags);
assertEquals(Integer.valueOf(10), data.limits.get("api"));
```

## Nested generics

```java
import java.util.List;
import java.util.Map;

class GenericBox<T> { public T value; }
class GenericItem<T> { public T payload; }
class StringConstructedGenericKey<T> {
    public final String value;
    public StringConstructedGenericKey(String value) { this.value = value; }
}
class GenericData {
    public GenericBox<List<GenericItem<String>>> foo;
    public Map<StringConstructedGenericKey<Integer>, List<String>> values;
}

String json = "{\"foo\":{\"value\":[{\"payload\":\"a\"}]},\"values\":{\"k1\":[\"x\",\"y\"]}}";
GenericData data = new JsonDeserializer().deserialize(json, GenericData.class);
```

## Class references

```java
interface Service {}
final class ServiceImpl implements Service {}
class DataModel {
    public Class<Service> impl;
}

String json = "{\"impl\":\"" + ServiceImpl.class.getName() + "\"}";
DataModel data = new JsonDeserializer().deserialize(json, DataModel.class);

assertEquals(ServiceImpl.class, data.impl);
```

## Null semantics

```java
import java.util.Optional;

class DataModel {
    public Optional<String> user = Optional.of("default-user");
}

DataModel data = new JsonDeserializer().deserialize("{\"user\":null}", DataModel.class);
assertEquals(Optional.empty(), data.user);
```

See [../../errors.md](../../errors.md) for error details.
