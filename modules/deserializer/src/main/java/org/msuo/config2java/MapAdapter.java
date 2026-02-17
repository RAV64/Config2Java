package org.msuo.config2java;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

final class MapAdapter implements TypeAdapter {

    private final Type keyType;
    private final Type valueType;

    MapAdapter(ParameterizedType pt) {
        this.keyType = pt.getActualTypeArguments()[0];
        this.valueType = pt.getActualTypeArguments()[1];
    }

    @Override
    public ReadResult read(Path path, ConfigValue value, ErrorCollector errors) {
        ConfigTable table = ValueCoerce.requireTable(path, value, errors, Errors.mapExpected(value));
        if (table == null) return ReadResult.fail();

        Map<Object, Object> out = new LinkedHashMap<>();

        for (ConfigEntry e : table.entries()) {
            ConfigValue k = e.key();
            ConfigValue v = e.value();

            Path keyPath = path.rawKey(e.rawKeyString());
            ReadResult keyRes = ObjectMapper.readValue(keyPath, keyType, k, errors);
            if (!keyRes.ok) continue;

            Object keyObj = keyRes.value;

            Path valPath = path.mapKey(keyObj);
            ReadResult valRes = ObjectMapper.readValue(valPath, valueType, v, errors);
            if (!valRes.ok) continue;

            out.put(keyObj, valRes.value);
        }

        return ReadResult.ok(out);
    }
}
