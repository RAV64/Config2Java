package org.msuo.data2java;

final class PrimitiveRejectedAdapter implements TypeAdapter {

    private final Class<?> primitive;

    PrimitiveRejectedAdapter(Class<?> primitive) {
        this.primitive = primitive;
    }

    @Override
    public ReadResult read(Path path, DataValue value, ErrorCollector errors) {
        errors.add(path, Errors.primitiveNotSupported(primitive));
        return ReadResult.fail();
    }
}
