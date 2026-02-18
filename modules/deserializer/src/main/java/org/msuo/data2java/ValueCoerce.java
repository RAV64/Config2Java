package org.msuo.data2java;

import java.util.function.Function;

final class ValueCoerce {

    private ValueCoerce() {}

    static DataTable requireTable(Path path, DataValue value, ErrorCollector errors, DataErrorType error) {
        if (!value.isTable()) {
            errors.add(path, error);
            return null;
        }
        return value.asTable();
    }

    static ScalarValue scalarOrError(Path path, DataValue value, ErrorCollector errors) {
        ScalarValue scalar = value.asScalar();
        if (scalar == null) {
            errors.add(path, Errors.expectedScalar(value));
        }
        return scalar;
    }

    static String stringOrError(
        Path path,
        DataValue value,
        ErrorCollector errors,
        Function<DataValue, DataErrorType> errorFactory
    ) {
        ScalarValue scalar = value.asScalar();
        if (scalar == null || scalar.boxedType != String.class) {
            errors.add(path, errorFactory.apply(value));
            return null;
        }
        return (String) scalar.value;
    }
}
