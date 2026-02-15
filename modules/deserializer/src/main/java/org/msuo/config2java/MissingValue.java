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
}
