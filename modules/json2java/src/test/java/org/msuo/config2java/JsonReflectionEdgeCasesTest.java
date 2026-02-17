package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class JsonReflectionEdgeCasesTest extends JsonContractSupport {

    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("{\"base\":\"x\",\"child\":1}", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("{\"n\":1}", PrimitiveFieldNotSupported.class);
        assertSingleError(ex, ConfigErrorTypes.PrimitiveNotSupported.class, "n");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        ConfigDeserializationException ex = fails("\"nope\"", RootIsComplex.class);
        assertSingleError(ex, ConfigErrorTypes.NoOneArgCtor.class);
    }

    @Test
    void unresolvedTypeVariableField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "{\"value\":\"x\"}",
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
            "{\"values\":[\"x\"]}",
            UnresolvedTypeVariableArrayField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "values");
    }

    @Test
    void wildcardNestedGenericField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "{\"foo\":{\"value\":\"x\"}}",
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
            "{\"impl\":\"" + ClassRefServiceImpl.class.getName() + "\"}",
            ClassReferenceField.class
        );
        assertEquals(ClassRefServiceImpl.class, cfg.impl);
    }

    @Test
    void classReferenceField_rejectsNonAssignableClass() {
        ConfigDeserializationException ex = fails(
            "{\"impl\":\"" + ClassRefOther.class.getName() + "\"}",
            ClassReferenceField.class
        );
        assertSingleError(ex, ConfigErrorTypes.ClassRefNotAssignable.class, "impl");
    }

    @Test
    void classReferenceField_rejectsUnknownClassName() {
        ConfigDeserializationException ex = fails(
            "{\"impl\":\"no.such.Type\"}",
            ClassReferenceField.class
        );
        assertSingleError(ex, ConfigErrorTypes.ClassRefNotFound.class, "impl");
    }

    @Test
    void classReferenceField_requiresStringValue() {
        ConfigDeserializationException ex = fails(
            "{\"impl\":1}",
            ClassReferenceField.class
        );
        assertSingleError(ex, ConfigErrorTypes.ClassRefExpectedString.class, "impl");
    }

    @Test
    void classReferenceField_acceptsSubclassForSuperclassField() {
        ClassReferenceSuperclassField cfg = ok(
            "{\"impl\":\"" + ClassRefDerived.class.getName() + "\"}",
            ClassReferenceSuperclassField.class
        );
        assertEquals(ClassRefDerived.class, cfg.impl);
    }
}
