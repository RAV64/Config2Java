package org.msuo.config2java;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

final class OptionalAdapter implements TypeAdapter {

    private final ParameterizedType pt;

    OptionalAdapter(ParameterizedType pt) {
        this.pt = pt;
    }

    @Override
    public ReadResult missing(Path path, ErrorCollector errors) {
        return ReadResult.ok(Optional.empty());
    }

    @Override
    public ReadResult read(Path path, ConfigValue value, ErrorCollector errors) {
        if (value.isMissing() || value.isNil()) return ReadResult.ok(Optional.empty());

        Class<?> inner = TypeUtils.requireConcreteClassArg(
            pt.getActualTypeArguments()[0],
            path,
            Errors.optionalInnerMustBeConcrete(pt.getActualTypeArguments()[0]),
            errors
        );
        if (inner == null) return ReadResult.ok(Optional.empty());

        ReadResult innerRes = ObjectMapper.readValue(path, inner, value, errors);
        if (!innerRes.ok) return ReadResult.fail();
        return ReadResult.ok(Optional.of(innerRes.value));
    }
}
