package org.msuo.data2java;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

final class CollectionAdapter implements TypeAdapter {

    private final Class<?> raw;
    private final Type elementType;

    CollectionAdapter(ParameterizedType pt, Class<?> raw) {
        this.raw = raw;
        this.elementType = pt.getActualTypeArguments()[0];
    }

    @Override
    public ReadResult read(Path path, DataValue value, ErrorCollector errors) {
        DataTable table = ValueCoerce.requireTable(path, value, errors, Errors.collectionExpected(raw, value));
        if (table == null) return ReadResult.fail();

        final boolean wantSet = Set.class.isAssignableFrom(raw);
        final Collection<Object> out = wantSet ? new LinkedHashSet<>() : new ArrayList<>();

        int n = table.length();
        for (int i = 1; i <= n; i++) {
            DataValue v = table.getIndex(i);
            Path elemPath = path.index(i);
            ReadResult rr = ObjectMapper.readValue(elemPath, elementType, v, errors);
            if (rr.ok) out.add(rr.value);
        }

        return ReadResult.ok(out);
    }
}
