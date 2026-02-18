package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CoreMissingDefaultsAndNestedTest extends CoreMapperContractSupport {

    @Test
    void missingRequired_reportsMissingRequiredField() {
        DataDeserializationException ex = fails(obj(), MissingRequired.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "name");
    }

    @Test
    void missingOptional_mapsToEmptyOptional() {
        MissingOptional data = ok(obj(), MissingOptional.class);
        assertEquals(java.util.Optional.empty(), data.name);
    }

    @Test
    void missingWithDefault_keepsDefaultValue() {
        DefaultValue data = ok(obj(), DefaultValue.class);
        assertEquals("default", data.name.value);
    }

    @Test
    void nestedObject_success() {
        NestedPortContainer data = ok(obj("db", obj("port", 5432)), NestedPortContainer.class);
        assertEquals(Integer.valueOf(5432), data.db.port.value);
    }

    @Test
    void nestedEmptyTable_missingRequiredNestedField() {
        DataDeserializationException ex = fails(obj("db", obj()), NestedPortContainer.class);
        assertSingleError(ex, DataErrorTypes.MissingRequiredField.class, "db", "port");
    }

    @Test
    void nestedNoNoArgCtor_reportsNoNoArgCtor() {
        DataDeserializationException ex = fails(
            obj("bad", obj("x", "x")),
            BadNestedNoNoArgContainer.class
        );
        assertSingleError(ex, DataErrorTypes.NoNoArgCtor.class, "bad");
    }

    @Test
    void unknownFields_areReported() {
        DataDeserializationException ex = fails(
            obj("name", "ok", "extra", 123, "other", obj("a", 1)),
            UnknownFieldInput.class
        );
        assertEquals(2, ex.getErrors().size());
        assertHasError(ex, DataErrorTypes.UnknownField.class, "extra");
        assertHasError(ex, DataErrorTypes.UnknownField.class, "other");
    }

    @Test
    void nestedUnknownField_isReportedAtNestedPath() {
        DataDeserializationException ex = fails(
            obj("db", obj("port", 5432, "extra", 1)),
            NestedPortContainer.class
        );
        assertSingleError(ex, DataErrorTypes.UnknownField.class, "db", "extra");
    }

    @Test
    void mixedKnownFailureAndUnknownField_areBothReported() {
        DataDeserializationException ex = fails(
            obj("name", "", "extra", 1),
            UnknownFieldInput.class
        );
        assertEquals(2, ex.getErrors().size());
        assertHasError(ex, DataErrorTypes.CtorRejected.class, "name");
        assertHasError(ex, DataErrorTypes.UnknownField.class, "extra");
    }

    @Test
    void inheritedFieldsPlusUnknownField_reportsOnlyUnknownField() {
        DataDeserializationException ex = fails(
            obj("base", "x", "child", 1, "extra", 1),
            DerivedData.class
        );
        assertSingleError(ex, DataErrorTypes.UnknownField.class, "extra");
    }

    @Test
    void emptyModel_withInputKeys_reportsUnknownFields() {
        DataDeserializationException ex = fails(
            obj("a", 1, "b", 2),
            EmptyData.class
        );
        assertEquals(2, ex.getErrors().size());
        assertHasError(ex, DataErrorTypes.UnknownField.class, "a");
        assertHasError(ex, DataErrorTypes.UnknownField.class, "b");
    }
}
