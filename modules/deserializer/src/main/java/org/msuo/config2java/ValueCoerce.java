package org.msuo.config2java;

final class ValueCoerce {

    private ValueCoerce() {}

    static ConfigTable requireTable(Path path, ConfigValue value, ErrorCollector errors, ConfigErrorType error) {
        if (!value.isTable()) {
            errors.add(path, error);
            return null;
        }
        return value.asTable();
    }

    static ScalarValue scalarOrError(Path path, ConfigValue value, ErrorCollector errors) {
        ScalarValue scalar = value.asScalar();
        if (scalar == null) {
            errors.add(path, Errors.expectedScalar(value));
        }
        return scalar;
    }
}
