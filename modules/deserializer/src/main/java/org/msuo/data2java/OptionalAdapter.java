package org.msuo.data2java;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

final class OptionalAdapter implements TypeAdapter {

    private final Type innerType;

    OptionalAdapter(ParameterizedType pt) {
        this.innerType = pt.getActualTypeArguments()[0];
    }

    @Override
    public ReadResult missing(Path path, ErrorCollector errors) {
        return ReadResult.ok(Optional.empty());
    }

    @Override
    public ReadResult read(Path path, DataValue value, ErrorCollector errors) {
        if (value.isMissing() || value.isNil()) return ReadResult.ok(Optional.empty());

        ReadResult innerRes = ObjectMapper.readValue(path, innerType, value, errors);
        if (!innerRes.ok) return ReadResult.fail();
        return ReadResult.ok(Optional.of(innerRes.value));
    }
}
