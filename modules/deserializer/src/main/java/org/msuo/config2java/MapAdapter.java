package org.msuo.config2java;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

final class MapAdapter implements TypeAdapter {

    private final ParameterizedType pt;

    MapAdapter(ParameterizedType pt) {
        this.pt = pt;
    }

    @Override
    public ReadResult read(Path path, ConfigValue value, ErrorCollector errors) {
        ConfigTable table = ValueCoerce.requireTable(path, value, errors, Errors.mapExpected(value));
        if (table == null) return ReadResult.fail();

        Type kType = pt.getActualTypeArguments()[0];
        Type vType = pt.getActualTypeArguments()[1];

        Class<?> kCls = TypeUtils.requireConcreteClassArg(
            kType,
            path,
            Errors.mapKeyMustBeConcrete(kType),
            errors
        );
        Class<?> vCls = TypeUtils.requireConcreteClassArg(
            vType,
            path,
            Errors.mapValueMustBeConcrete(vType),
            errors
        );
        if (kCls == null || vCls == null) return ReadResult.fail();

        Map<Object, Object> out = new LinkedHashMap<>();

        for (ConfigEntry e : table.entries()) {
            ConfigValue k = e.key();
            ConfigValue v = e.value();

            Path keyPath = path.rawKey(e.rawKeyString());
            ReadResult keyRes = ObjectMapper.readValue(keyPath, kCls, k, errors);
            if (!keyRes.ok) continue;

            Object keyObj = keyRes.value;

            Path valPath = path.mapKey(keyObj);
            ReadResult valRes = ObjectMapper.readValue(valPath, vCls, v, errors);
            if (!valRes.ok) continue;

            out.put(keyObj, valRes.value);
        }

        return ReadResult.ok(out);
    }
}
