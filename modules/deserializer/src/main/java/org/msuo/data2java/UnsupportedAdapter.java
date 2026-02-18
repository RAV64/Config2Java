package org.msuo.data2java;

final class UnsupportedAdapter implements TypeAdapter {

    private final DataErrorType error;

    UnsupportedAdapter(DataErrorType error) {
        this.error = error;
    }

    @Override
    public ReadResult read(Path path, DataValue value, ErrorCollector errors) {
        errors.add(path, error);
        return ReadResult.fail();
    }
}
