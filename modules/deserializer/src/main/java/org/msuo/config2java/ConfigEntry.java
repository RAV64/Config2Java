package org.msuo.config2java;

final class ConfigEntry {

    private final ConfigValue key;
    private final ConfigValue value;
    private final String rawKeyString;

    private ConfigEntry(ConfigValue key, ConfigValue value, String rawKeyString) {
        this.key = key;
        this.value = value;
        this.rawKeyString = rawKeyString;
    }

    static ConfigEntry of(ConfigValue key, ConfigValue value, String rawKeyString) {
        return new ConfigEntry(key, value, rawKeyString);
    }

    ConfigValue key() {
        return key;
    }

    ConfigValue value() {
        return value;
    }

    String rawKeyString() {
        return rawKeyString;
    }
}
