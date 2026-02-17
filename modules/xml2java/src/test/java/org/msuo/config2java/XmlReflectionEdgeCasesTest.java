package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
public class XmlReflectionEdgeCasesTest extends XmlContractSupport {

    @Test
    void inheritedFields_areDeserialized() {
        DerivedCfg cfg = ok("<config><base>x</base><child>1</child></config>", DerivedCfg.class);
        assertEquals("x", cfg.base.value);
        assertEquals(Integer.valueOf(1), cfg.child.value);
    }

    @Test
    void primitiveFieldTypes_areRejected() {
        ConfigDeserializationException ex = fails("<config><n>1</n></config>", PrimitiveFieldNotSupported.class);
        assertSingleError(ex, ConfigErrorTypes.PrimitiveNotSupported.class, "n");
    }

    @Test
    void rootNotATable_forComplexConfig_fails() {
        ConfigSourceException ex = assertThrows(ConfigSourceException.class, () ->
            deserialize("<config>", RootIsComplex.class)
        );
        assertEquals("XML", ex.format());
        assertEquals("parse", ex.phase());
    }

    @Test
    void unresolvedTypeVariableField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "<config><value>x</value></config>",
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
            "<config><values>x</values></config>",
            UnresolvedTypeVariableArrayField.class
        );
        assertSingleError(ex, ConfigErrorTypes.UnsupportedType.class, "values");
    }

    @Test
    void wildcardNestedGenericField_reportsUnsupportedType() {
        ConfigDeserializationException ex = fails(
            "<config><foo><value>x</value></foo></config>",
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
            "<config><impl>" + ClassRefServiceImpl.class.getName() + "</impl></config>",
            ClassReferenceField.class
        );
        assertEquals(ClassRefServiceImpl.class, cfg.impl);
    }

    @Test
    void classReferenceField_rejectsNonAssignableClass() {
        ConfigDeserializationException ex = fails(
            "<config><impl>" + ClassRefOther.class.getName() + "</impl></config>",
            ClassReferenceField.class
        );
        assertSingleError(ex, ConfigErrorTypes.ClassRefNotAssignable.class, "impl");
    }

    @Test
    void classReferenceField_rejectsUnknownClassName() {
        ConfigDeserializationException ex = fails(
            "<config><impl>no.such.Type</impl></config>",
            ClassReferenceField.class
        );
        assertSingleError(ex, ConfigErrorTypes.ClassRefNotFound.class, "impl");
    }

    @Test
    void classReferenceField_requiresStringValue() {
        ConfigDeserializationException ex = fails(
            "<config><impl><x>1</x></impl></config>",
            ClassReferenceField.class
        );
        assertSingleError(ex, ConfigErrorTypes.ClassRefExpectedString.class, "impl");
    }

    @Test
    void classReferenceField_acceptsSubclassForSuperclassField() {
        ClassReferenceSuperclassField cfg = ok(
            "<config><impl>" + ClassRefDerived.class.getName() + "</impl></config>",
            ClassReferenceSuperclassField.class
        );
        assertEquals(ClassRefDerived.class, cfg.impl);
    }
}
