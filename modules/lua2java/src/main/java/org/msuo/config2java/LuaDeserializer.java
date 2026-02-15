package org.msuo.config2java;

import java.util.Map;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

public final class LuaDeserializer extends TreeDeserializer {

    private final ScriptBindings bindings;

    public LuaDeserializer() {
        this(ScriptBindings.empty());
    }

    public LuaDeserializer(ScriptBindings bindings) {
        this.bindings = bindings;
    }

    public static ScriptedDeserializerBuilder<LuaDeserializer> builder() {
        return ScriptedDeserializerBuilder.of(LuaDeserializer::new);
    }

    @Override
    protected ConfigValue parse(String source) {
        try {
            Globals runtime = JsePlatform.standardGlobals();
            injectEnvironment(runtime);
            injectGlobals(runtime);
            LuaValue root = runtime.load(source).call();
            return new LuaConfigValue(root);
        } catch (RuntimeException e) {
            throw new ConfigSourceException("Lua", "evaluate", e.getMessage(), e);
        }
    }

    private void injectEnvironment(Globals runtime) {
        runtime.set(
            "ENV",
            toLuaValue(EnvironmentValues.withSystemFallback(bindings.environment()))
        );

        LuaValue os = runtime.get("os");
        if (!os.istable()) {
            return;
        }

        LuaTable osTable = os.checktable();
        LuaValue originalGetenv = osTable.get("getenv");
        osTable.set(
            "getenv",
            new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    String key = arg.tojstring();
                    if (bindings.environment().containsKey(key)) {
                        String value = bindings.environment().get(key);
                        return value == null ? LuaValue.NIL : LuaValue.valueOf(value);
                    }

                    if (!originalGetenv.isnil()) {
                        return originalGetenv.call(arg);
                    }

                    String system = System.getenv(key);
                    return system == null ? LuaValue.NIL : LuaValue.valueOf(system);
                }
            }
        );
    }

    private void injectGlobals(Globals runtime) {
        for (Map.Entry<String, Object> e : bindings.globals().entrySet()) {
            runtime.set(e.getKey(), toLuaValue(e.getValue()));
        }
    }

    private static LuaValue toLuaValue(Object value) {
        if (value == null) return LuaValue.NIL;
        if (value instanceof LuaValue) return (LuaValue) value;
        if (value instanceof String) return LuaValue.valueOf((String) value);
        if (value instanceof Boolean) return LuaValue.valueOf((Boolean) value);
        if (value instanceof Number) {
            Number n = (Number) value;
            double d = n.doubleValue();
            if (ScalarNumbers.fitsInt(d)) {
                return LuaValue.valueOf((int) d);
            }
            return LuaValue.valueOf(d);
        }
        if (value instanceof Map<?, ?>) {
            LuaTable table = new LuaTable();
            Map<?, ?> map = (Map<?, ?>) value;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                table.set(toLuaValue(e.getKey()), toLuaValue(e.getValue()));
            }
            return table;
        }
        if (value instanceof Iterable<?>) {
            LuaTable table = new LuaTable();
            int index = 1;
            for (Object item : (Iterable<?>) value) {
                table.set(index++, toLuaValue(item));
            }
            return table;
        }

        throw new IllegalArgumentException(
            "Unsupported global value type for Lua injection: " + value.getClass().getName()
        );
    }

    private static final class LuaConfigValue implements ConfigValue {

        private final LuaValue value;

        LuaConfigValue(LuaValue value) {
            this.value = value;
        }

        @Override
        public String typename() {
            switch (value.type()) {
                case LuaValue.TNIL:
                    return "nil";
                case LuaValue.TTABLE:
                    return "table";
                case LuaValue.TSTRING:
                    return "string";
                case LuaValue.TBOOLEAN:
                    return "boolean";
                case LuaValue.TNUMBER:
                    return "number";
                default:
                    return "userdata";
            }
        }

        @Override
        public boolean isNil() {
            return value.isnil();
        }

        @Override
        public boolean isTable() {
            return value.istable();
        }

        @Override
        public ConfigTable asTable() {
            return new LuaConfigTable(value.checktable());
        }

        @Override
        public ScalarValue asScalar() {
            if (value.isnil()) return null;
            if (value.isnumber()) {
                if (value.isint()) return ScalarValue.ofInt(value.toint());
                return ScalarValue.ofDouble(value.todouble());
            }
            if (value.isboolean()) return ScalarValue.ofBoolean(value.toboolean());
            if (value.isstring()) return ScalarValue.ofString(value.tojstring());
            return null;
        }
    }

    private static final class LuaConfigTable implements ConfigTable {

        private final LuaTable table;

        LuaConfigTable(LuaTable table) {
            this.table = table;
        }

        @Override
        public ConfigValue getField(String key) {
            LuaValue v = table.get(key);
            if (v.isnil()) return MissingValue.INSTANCE;
            return new LuaConfigValue(v);
        }

        @Override
        public int length() {
            return table.length();
        }

        @Override
        public ConfigValue getIndex(int index1Based) {
            LuaValue v = table.get(index1Based);
            if (v.isnil()) return MissingValue.INSTANCE;
            return new LuaConfigValue(v);
        }

        @Override
        public Iterable<ConfigEntry> entries() {
            return () -> new java.util.Iterator<ConfigEntry>() {
                private LuaValue key = LuaValue.NIL;
                private Varargs next = table.next(LuaValue.NIL);

                @Override
                public boolean hasNext() {
                    return !next.arg1().isnil();
                }

                @Override
                public ConfigEntry next() {
                    key = next.arg1();
                    LuaValue value = next.arg(2);
                    next = table.next(key);
                    ConfigValue keyValue = key.isnil()
                        ? MissingValue.INSTANCE
                        : new LuaConfigValue(key);
                    return ConfigEntry.of(
                        keyValue,
                        new LuaConfigValue(value),
                        key.tojstring()
                    );
                }
            };
        }
    }

}
