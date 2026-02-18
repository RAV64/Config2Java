package org.msuo.data2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class LuaReflectionEdgeCasesTest extends LuaContractSupport {

    @Test
    void inheritedFields_areDeserialized() {
        DerivedData data = ok("return { base = 'x', child = 1 }", DerivedData.class);
        assertEquals("x", data.base.value);
        assertEquals(Integer.valueOf(1), data.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        DataDeserializationException ex = fails("return { n = 1 }", PrimitiveFieldNotSupported.class);
        assertSingleError(ex, DataErrorTypes.PrimitiveNotSupported.class, "n");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        DataDeserializationException ex = fails("return 'nope'", RootIsComplex.class);
        assertSingleError(ex, DataErrorTypes.NoOneArgCtor.class);
    }

    @Test
    void unresolvedTypeVariableField_reportsUnsupportedType() {
        DataDeserializationException ex = fails(
            "return { value = 'x' }",
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
            "return { values = { 'x' } }",
            UnresolvedTypeVariableArrayField.class
        );
        assertSingleError(ex, DataErrorTypes.UnsupportedType.class, "values");
    }

    @Test
    void wildcardNestedGenericField_reportsUnsupportedType() {
        DataDeserializationException ex = fails(
            "return { foo = { value = 'x' } }",
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
            "return { impl = '" + ClassRefServiceImpl.class.getName() + "' }",
            ClassReferenceField.class
        );
        assertEquals(ClassRefServiceImpl.class, data.impl);
    }

    @Test
    void classReferenceField_rejectsNonAssignableClass() {
        DataDeserializationException ex = fails(
            "return { impl = '" + ClassRefOther.class.getName() + "' }",
            ClassReferenceField.class
        );
        assertSingleError(ex, DataErrorTypes.ClassRefNotAssignable.class, "impl");
    }

    @Test
    void classReferenceField_rejectsUnknownClassName() {
        DataDeserializationException ex = fails(
            "return { impl = 'no.such.Type' }",
            ClassReferenceField.class
        );
        assertSingleError(ex, DataErrorTypes.ClassRefNotFound.class, "impl");
    }

    @Test
    void classReferenceField_requiresStringValue() {
        DataDeserializationException ex = fails(
            "return { impl = 1 }",
            ClassReferenceField.class
        );
        assertSingleError(ex, DataErrorTypes.ClassRefExpectedString.class, "impl");
    }

    @Test
    void classReferenceField_acceptsSubclassForSuperclassField() {
        ClassReferenceSuperclassField data = ok(
            "return { impl = '" + ClassRefDerived.class.getName() + "' }",
            ClassReferenceSuperclassField.class
        );
        assertEquals(ClassRefDerived.class, data.impl);
    }
}
