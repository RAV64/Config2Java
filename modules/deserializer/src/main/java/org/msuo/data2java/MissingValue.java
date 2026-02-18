package org.msuo.data2java;

final class MissingValue implements DataValue {

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
