package org.msuo.config2java;

interface TypeAdapter {
    ReadResult read(Path path, ConfigValue value, ErrorCollector errors);

    default ReadResult missing(Path path, ErrorCollector errors) {
        errors.add(path, Errors.missingRequiredField());
        return ReadResult.fail();
    }
}
