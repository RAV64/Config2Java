package org.msuo.config2java;

final class UnsupportedAdapter implements TypeAdapter {

    private final ConfigErrorType error;

    UnsupportedAdapter(ConfigErrorType error) {
        this.error = error;
    }

    @Override
    public ReadResult read(Path path, ConfigValue value, ErrorCollector errors) {
        errors.add(path, error);
        return ReadResult.fail();
    }
}
