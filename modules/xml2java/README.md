# xml2java

XML to Java deserializer for Data2Java.

Depends on:
- [../deserializer](../deserializer/README.md)
- no extra runtime dependency (JDK XML parser)

```gradle
implementation "org.msuo:xml2java:<version>"
```

## Leaf values

```java
class DataModel {
    public String name;
    public Integer port;
}

String xml = "<data><name>svc</name><port>8080</port></data>";
DataModel data = new XmlDeserializer().deserialize(xml, DataModel.class);

assertEquals("svc", data.name);
assertEquals(Integer.valueOf(8080), data.port);
```

## Missing keys keep defaults (unknown keys fail)

Missing keys preserve defaults. Unknown keys are reported as `UnknownField` errors.


```java
class DataModel {
    public String name = "default-name";
    public Integer port;
}

DataModel data = new XmlDeserializer().deserialize("<data><port>8080</port></data>", DataModel.class);
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

String xml = "<data><db><host>db</host><port>15432</port></db></data>";
DataModel data = new XmlDeserializer().deserialize(xml, DataModel.class);

assertEquals("db", data.db.host);
assertEquals(Integer.valueOf(15432), data.db.port);
```

## Optionals

```java
import java.util.Optional;

class DataModel {
    public Optional<String> user = Optional.of("default-user");
}

DataModel present = new XmlDeserializer().deserialize("<data><user>alice</user></data>", DataModel.class);
DataModel missing = new XmlDeserializer().deserialize("<data/>", DataModel.class);

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

String xml = "<data><tags>a</tags><tags>b</tags><limits><api>10</api></limits></data>";
DataModel data = new XmlDeserializer().deserialize(xml, DataModel.class);

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

String xml = "<data><foo><value><payload>a</payload></value></foo><values><k1>x</k1><k1>y</k1></values></data>";
GenericData data = new XmlDeserializer().deserialize(xml, GenericData.class);
```

## Class references

```java
interface Service {}
final class ServiceImpl implements Service {}
class DataModel {
    public Class<Service> impl;
}

String xml = "<data><impl>" + ServiceImpl.class.getName() + "</impl></data>";
DataModel data = new XmlDeserializer().deserialize(xml, DataModel.class);

assertEquals(ServiceImpl.class, data.impl);
```

## Optional semantics

```java
import java.util.Optional;

class DataModel {
    public Optional<String> user = Optional.of("default-user");
}

// XML has no explicit null literal. Omitted key behaves as missing.
DataModel data = new XmlDeserializer().deserialize("<data/>", DataModel.class);
assertEquals(Optional.of("default-user"), data.user);
```

## XML text content behavior

Structured elements (elements that also contain child elements) may expose mixed text content as a `text` field.
If your Java target type does not define a `text` field for that object, deserialization reports `UnknownField` for that node.

See [../../errors.md](../../errors.md) for error details.
