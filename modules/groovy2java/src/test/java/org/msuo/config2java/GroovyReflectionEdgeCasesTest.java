package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class GroovyReflectionEdgeCasesTest extends GroovyContractSupport {

    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("return [base: 'x', child: 1]", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("return [n: 1]", PrimitiveFieldNotSupported.class);
        assertSingleError(ex, ConfigErrorTypes.PrimitiveNotSupported.class, "n");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        ConfigDeserializationException ex = fails("return 'nope'", RootIsComplex.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class);
    }

    @Test
    void unresolvedTypeVariableField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "return [value: 'x']",
            UnresolvedTypeVariableField.class
        );
        assertSingleError(
            ex,
            ConfigErrorTypes.UnresolvedTypeVariable.class,
            "value"
        );
    }

    @Test
    void unresolvedTypeVariableArrayField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "return [values: ['x']]",
            UnresolvedTypeVariableArrayField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "values");
    }

    @Test
    void wildcardNestedGenericField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "return [foo: [value: 'x']]",
            WildcardGenericNestedField.class
        );
        assertSingleError(
            ex,
            ConfigErrorTypes.WildcardTypeNotSupported.class,
            "foo",
            "value"
        );
    }

    @Test
    void classReferenceField_resolvesAssignableClass() {
        ClassReferenceField cfg = ok(
            "return [impl: '" + ClassRefServiceImpl.class.getName() + "']",
            ClassReferenceField.class
        );
        assertEquals(ClassRefServiceImpl.class, cfg.impl);
    }

    @Test
    void classReferenceField_rejectsNonAssignableClass() {
        ConfigDeserializationException ex = fails(
            "return [impl: '" + ClassRefOther.class.getName() + "']",
            ClassReferenceField.class
        );
        assertSingleError(ex, ConfigErrorTypes.ClassRefNotAssignable.class, "impl");
    }

    @Test
    void classReferenceField_requiresStringValue() {
        ConfigDeserializationException ex = fails(
            "return [impl: 1]",
            ClassReferenceField.class
        );
        assertSingleError(ex, ConfigErrorTypes.ClassRefExpectedString.class, "impl");
    }

    @Test
    void classReferenceField_acceptsSubclassForSuperclassField() {
        ClassReferenceSuperclassField cfg = ok(
            "return [impl: '" + ClassRefDerived.class.getName() + "']",
            ClassReferenceSuperclassField.class
        );
        assertEquals(ClassRefDerived.class, cfg.impl);
    }
}
