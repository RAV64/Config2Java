package org.msuo.data2java;

import java.util.Map;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

public final class LuaDeserializer extends AbstractScriptDeserializer {

    @Override
    protected DataValue parse(
        String source,
        Map<String, String> environment,
        Map<String, Object> globals
    ) {
        try {
            Globals runtime = JsePlatform.standardGlobals();
            injectEnvironment(runtime, environment);
            injectGlobals(runtime, globals);
            LuaValue root = runtime.load(source).call();
            return new LuaDataValue(root);
        } catch (RuntimeException e) {
            throw new DataSourceException("Lua", "evaluate", e.getMessage(), e);
        }
    }

    private static void injectEnvironment(Globals runtime, Map<String, String> environment) {
        Map<String, String> resolvedEnvironment = EnvironmentValues.withSystemFallback(
            environment
        );
        runtime.set(
            "ENV",
            toLuaValue(resolvedEnvironment)
        );

        LuaValue os = runtime.get("os");
        if (!os.istable()) {
            return;
        }

        LuaTable osTable = os.checktable();
        osTable.set(
            "getenv",
            new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    String key = arg.tojstring();
                    if (resolvedEnvironment.containsKey(key)) {
                        String value = resolvedEnvironment.get(key);
                        return value == null ? LuaValue.NIL : LuaValue.valueOf(value);
                    }
                    return LuaValue.NIL;
                }
            }
        );
    }

    private static void injectGlobals(Globals runtime, Map<String, Object> globals) {
        for (Map.Entry<String, Object> e : globals.entrySet()) {
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

    private static final class LuaDataValue implements DataValue {

        private final LuaValue value;

        LuaDataValue(LuaValue value) {
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
        public DataTable asTable() {
            return new LuaDataTable(value.checktable());
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

    private static final class LuaDataTable implements DataTable {

        private final LuaTable table;

        LuaDataTable(LuaTable table) {
            this.table = table;
        }

        @Override
        public DataValue getField(String key) {
            LuaValue v = table.get(key);
            if (v.isnil()) return MissingValue.INSTANCE;
            return new LuaDataValue(v);
        }

        @Override
        public int length() {
            return table.length();
        }

        @Override
        public DataValue getIndex(int index1Based) {
            LuaValue v = table.get(index1Based);
            if (v.isnil()) return MissingValue.INSTANCE;
            return new LuaDataValue(v);
        }

        @Override
        public Iterable<DataEntry> entries() {
            return () -> new java.util.Iterator<DataEntry>() {
                private LuaValue key = LuaValue.NIL;
                private Varargs next = table.next(LuaValue.NIL);

                @Override
                public boolean hasNext() {
                    return !next.arg1().isnil();
                }

                @Override
                public DataEntry next() {
                    key = next.arg1();
                    LuaValue value = next.arg(2);
                    next = table.next(key);
                    DataValue keyValue = key.isnil()
                        ? MissingValue.INSTANCE
                        : new LuaDataValue(key);
                    return DataEntry.of(
                        keyValue,
                        new LuaDataValue(value),
                        key.tojstring()
                    );
                }
            };
        }
    }

}
