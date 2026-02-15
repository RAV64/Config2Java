package org.msuo.config2java;

final class EnumAdapter implements TypeAdapter {

    private final Class<?> enumClass;

    EnumAdapter(Class<?> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public ReadResult read(Path path, ConfigValue value, ErrorCollector errors) {
        ScalarValue scalar = value.asScalar();
        if (scalar == null || scalar.boxedType != String.class) {
            errors.add(path, Errors.enumExpectedString(value));
            return ReadResult.fail();
        }

        String name = (String) scalar.value;
        try {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Object e = Enum.valueOf((Class<? extends Enum>) enumClass, name);
            return ReadResult.ok(e);
        } catch (IllegalArgumentException ex) {
            errors.add(path, Errors.enumUnknown(enumClass, name));
            return ReadResult.fail();
        }
    }
}
