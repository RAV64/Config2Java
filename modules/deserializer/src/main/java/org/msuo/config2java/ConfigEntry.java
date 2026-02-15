package org.msuo.config2java;

interface ConfigEntry {
    ConfigValue key();

    ConfigValue value();

    String rawKeyString();

    static ConfigEntry of(ConfigValue key, ConfigValue value, String rawKeyString) {
        return new SimpleConfigEntry(key, value, rawKeyString);
    }

    final class SimpleConfigEntry implements ConfigEntry {
        private final ConfigValue key;
        private final ConfigValue value;
        private final String rawKeyString;

        SimpleConfigEntry(ConfigValue key, ConfigValue value, String rawKeyString) {
            this.key = key;
            this.value = value;
            this.rawKeyString = rawKeyString;
        }

        @Override
        public ConfigValue key() {
            return key;
        }

        @Override
        public ConfigValue value() {
            return value;
        }

        @Override
        public String rawKeyString() {
            return rawKeyString;
        }
    }
}
