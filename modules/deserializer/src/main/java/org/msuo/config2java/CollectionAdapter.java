package org.msuo.config2java;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

final class CollectionAdapter implements TypeAdapter {

    private final ParameterizedType pt;
    private final Class<?> raw;

    CollectionAdapter(ParameterizedType pt, Class<?> raw) {
        this.pt = pt;
        this.raw = raw;
    }

    @Override
    public ReadResult read(Path path, ConfigValue value, ErrorCollector errors) {
        ConfigTable table = ValueCoerce.requireTable(path, value, errors, Errors.collectionExpected(raw, value));
        if (table == null) return ReadResult.fail();

        Class<?> elemCls = TypeUtils.requireConcreteClassArg(
            pt.getActualTypeArguments()[0],
            path,
            Errors.collectionElementMustBeConcrete(pt.getActualTypeArguments()[0]),
            errors
        );
        if (elemCls == null) return ReadResult.fail();

        final boolean wantSet = Set.class.isAssignableFrom(raw);
        final Collection<Object> out = wantSet ? new LinkedHashSet<>() : new ArrayList<>();

        int n = table.length();
        for (int i = 1; i <= n; i++) {
            ConfigValue v = table.getIndex(i);
            Path elemPath = path.index(i);
            ReadResult rr = Binder.readValue(elemPath, elemCls, v, errors);
            if (rr.ok) out.add(rr.value);
        }

        return ReadResult.ok(out);
    }
}
