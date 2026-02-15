# xml2java

XML implementation for Config2Java.

## Depends on

- [../deserializer](../deserializer/README.md)
- no extra runtime dependency (JDK XML parser)

## Usage

```java
import org.msuo.config2java.XmlDeserializer;

String xml = """
<config>
  <app>
    <host>localhost</host>
    <port>8080</port>
  </app>
  <mode>DEV</mode>
</config>
""";

MyCfg cfg = new XmlDeserializer().deserialize(xml, MyCfg.class);
```

## XML mapping notes

- root element children are treated as top-level config fields
- repeated sibling elements map to lists
- attributes map to fields on that element object
- mixed content with attributes/children exposes text as `text`

Shared errors: [../../errors.md](../../errors.md).
