package org.msuo.config2java;

final class PrimitiveRejectedAdapter implements TypeAdapter {

    private final Class<?> primitive;

    PrimitiveRejectedAdapter(Class<?> primitive) {
        this.primitive = primitive;
    }

    @Override
    public ReadResult read(Path path, ConfigValue value, ErrorCollector errors) {
        errors.add(path, Errors.primitiveNotSupported(primitive));
        return ReadResult.fail();
    }
}
