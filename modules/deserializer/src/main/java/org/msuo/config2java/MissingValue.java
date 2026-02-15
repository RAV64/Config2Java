package org.msuo.config2java;

final class MissingValue implements ConfigValue {

    static final MissingValue INSTANCE = new MissingValue();

    private MissingValue() {}

    @Override
    public String typename() {
        return "missing";
    }

    @Override
    public boolean isMissing() {
        return true;
    }

    @Override
    public boolean isNil() {
        return false;
    }

    @Override
    public boolean isTable() {
        return false;
    }

    @Override
    public ConfigTable asTable() {
        throw new IllegalStateException("missing value cannot be treated as table");
    }

    @Override
    public ScalarValue asScalar() {
        return null;
    }
}
