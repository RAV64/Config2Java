package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlReflectionEdgeCasesTest extends XmlContractSupport {

    @Test
    void inheritedFields_areDeserialized() {
        DerivedData data = ok("<data><base>x</base><child>1</child></data>", DerivedData.class);
        assertEquals("x", data.base.value);
        assertEquals(Integer.valueOf(1), data.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        DataDeserializationException ex = fails("<data><n>1</n></data>", PrimitiveFieldNotSupported.class);
        assertSingleError(ex, DataErrorTypes.PrimitiveNotSupported.class, "n");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        DataSourceException ex = assertThrows(DataSourceException.class, () ->
            deserialize("<data>", RootIsComplex.class)
        );
        assertEquals("XML", ex.format());
        assertEquals("parse", ex.phase());
    }

    @Test
    void unresolvedTypeVariableField_reportsUnsupportedType() {
        DataDeserializationException ex = fails(
            "<data><value>x</value></data>",
            UnresolvedTypeVariableField.class
        );
        assertSingleError(
            ex,
            DataErrorTypes.UnresolvedTypeVariable.class,
            "value"
        );
    }

    @Test
    void unresolvedTypeVariableArrayField_reportsUnsupportedType() {
        DataDeserializationException ex = fails(
            "<data><values>x</values></data>",
            UnresolvedTypeVariableArrayField.class
        );
        assertSingleError(ex, DataErrorTypes.UnsupportedType.class, "values");
    }

    @Test
    void wildcardNestedGenericField_reportsUnsupportedType() {
        DataDeserializationException ex = fails(
            "<data><foo><value>x</value></foo></data>",
            WildcardGenericNestedField.class
        );
        assertSingleError(
            ex,
            DataErrorTypes.WildcardTypeNotSupported.class,
            "foo",
            "value"
        );
    }

    @Test
    void classReferenceField_resolvesAssignableClass() {
        ClassReferenceField data = ok(
            "<data><impl>" + ClassRefServiceImpl.class.getName() + "</impl></data>",
            ClassReferenceField.class
        );
        assertEquals(ClassRefServiceImpl.class, data.impl);
    }

    @Test
    void classReferenceField_rejectsNonAssignableClass() {
        DataDeserializationException ex = fails(
            "<data><impl>" + ClassRefOther.class.getName() + "</impl></data>",
            ClassReferenceField.class
        );
        assertSingleError(ex, DataErrorTypes.ClassRefNotAssignable.class, "impl");
    }

    @Test
    void classReferenceField_rejectsUnknownClassName() {
        DataDeserializationException ex = fails(
            "<data><impl>no.such.Type</impl></data>",
            ClassReferenceField.class
        );
        assertSingleError(ex, DataErrorTypes.ClassRefNotFound.class, "impl");
    }

    @Test
    void classReferenceField_requiresStringValue() {
        DataDeserializationException ex = fails(
            "<data><impl><x>1</x></impl></data>",
            ClassReferenceField.class
        );
        assertSingleError(ex, DataErrorTypes.ClassRefExpectedString.class, "impl");
    }

    @Test
    void classReferenceField_acceptsSubclassForSuperclassField() {
        ClassReferenceSuperclassField data = ok(
            "<data><impl>" + ClassRefDerived.class.getName() + "</impl></data>",
            ClassReferenceSuperclassField.class
        );
        assertEquals(ClassRefDerived.class, data.impl);
    }
}
