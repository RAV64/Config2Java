package org.msuo.config2java;

final class EnumAdapter implements TypeAdapter {

    private final Class<?> enumClass;

    EnumAdapter(Class<?> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public ReadResult read(Path path, ConfigValue value, ErrorCollector errors) {
        String name = ValueCoerce.stringOrError(
            path,
            value,
            errors,
            Errors::enumExpectedString
        );
        if (name == null) return ReadResult.fail();
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
