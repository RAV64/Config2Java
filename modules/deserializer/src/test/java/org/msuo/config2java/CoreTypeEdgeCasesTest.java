package org.msuo.config2java;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.junit.jupiter.api.Test;

public class CoreTypeEdgeCasesTest {

    static final class GenericHolder<T> {}

    @Test
    void unsupportedParameterizedRaw_reportsTypedError() {
        Type rawTypeVariable = GenericHolder.class.getTypeParameters()[0];
        ParameterizedType weird = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { String.class };
            }

            @Override
            public Type getRawType() {
                return rawTypeVariable;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };

        ErrorCollector errors = new ErrorCollector();
        ReadResult rr = ObjectMapper
            .adapterFor(weird)
            .read(Path.root(), new JavaScalarConfigValue("x"), errors);

        assertFalse(rr.ok);
        assertEquals(1, errors.asList().size());
        assertEquals(
            ConfigErrorTypes.UnsupportedParameterizedRaw.class,
            errors.asList().get(0).getErrorType().getClass()
        );
    }

    @Test
    void unsupportedType_fromTypeVariable_reportsTypedError() {
        Type typeVariable = GenericHolder.class.getTypeParameters()[0];

        ErrorCollector errors = new ErrorCollector();
        ReadResult rr = ObjectMapper
            .adapterFor(typeVariable)
            .read(Path.root(), new JavaScalarConfigValue("x"), errors);

        assertFalse(rr.ok);
        assertEquals(1, errors.asList().size());
        assertEquals(
            ConfigErrorTypes.UnsupportedType.class,
            errors.asList().get(0).getErrorType().getClass()
        );
    }
}
