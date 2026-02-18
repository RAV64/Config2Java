package org.msuo.data2java;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;

final class GenericTypeResolver {

    private GenericTypeResolver() {}

    static Type resolve(Type contextType, Class<?> contextRawType, Type toResolve) {
        while (toResolve instanceof TypeVariable<?>) {
            TypeVariable<?> typeVar = (TypeVariable<?>) toResolve;
            Type resolved = resolveTypeVariable(contextType, contextRawType, typeVar);
            if (resolved == typeVar) return resolved;
            toResolve = resolved;
        }

        if (toResolve instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) toResolve;
            Type owner = p.getOwnerType();
            Type newOwner = owner == null
                ? null
                : resolve(contextType, contextRawType, owner);

            Type[] args = p.getActualTypeArguments();
            Type[] newArgs = new Type[args.length];
            for (int i = 0; i < args.length; i++) {
                newArgs[i] = resolve(contextType, contextRawType, args[i]);
            }
            return new ParameterizedTypeImpl(
                p.getRawType(),
                newOwner,
                newArgs
            );
        }

        if (toResolve instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) toResolve;
            Type component = resolve(
                contextType,
                contextRawType,
                arrayType.getGenericComponentType()
            );
            return new GenericArrayTypeImpl(component);
        }

        if (toResolve instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) toResolve;
            Type[] lower = wildcard.getLowerBounds();
            Type[] upper = wildcard.getUpperBounds();
            if (lower.length == 1) {
                Type resolved = resolve(contextType, contextRawType, lower[0]);
                return new WildcardTypeImpl(
                    new Type[] { Object.class },
                    new Type[] { resolved }
                );
            }
            if (upper.length == 1) {
                Type resolved = resolve(contextType, contextRawType, upper[0]);
                return new WildcardTypeImpl(new Type[] { resolved }, new Type[0]);
            }
        }

        return toResolve;
    }

    private static Type resolveTypeVariable(
        Type contextType,
        Class<?> contextRawType,
        TypeVariable<?> unknown
    ) {
        Class<?> declaredByRaw = declaringClassOf(unknown);
        if (declaredByRaw == null) return unknown;

        Type declaredBy = getGenericSupertype(
            contextType,
            contextRawType,
            declaredByRaw
        );
        if (!(declaredBy instanceof ParameterizedType)) return unknown;

        ParameterizedType p = (ParameterizedType) declaredBy;
        TypeVariable<?>[] vars = declaredByRaw.getTypeParameters();
        for (int i = 0; i < vars.length; i++) {
            if (unknown.equals(vars[i])) {
                return p.getActualTypeArguments()[i];
            }
        }
        return unknown;
    }

    private static Type getGenericSupertype(
        Type context,
        Class<?> rawType,
        Class<?> toResolve
    ) {
        if (toResolve == rawType) return context;

        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            Type[] genericInterfaces = rawType.getGenericInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i] == toResolve) {
                    return genericInterfaces[i];
                }
                if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(
                        genericInterfaces[i],
                        interfaces[i],
                        toResolve
                    );
                }
            }
        }

        if (!rawType.isInterface()) {
            while (rawType != Object.class) {
                Class<?> rawSuper = rawType.getSuperclass();
                Type genericSuper = rawType.getGenericSuperclass();
                if (rawSuper == null) break;
                if (rawSuper == toResolve) {
                    return genericSuper;
                }
                if (toResolve.isAssignableFrom(rawSuper)) {
                    return getGenericSupertype(genericSuper, rawSuper, toResolve);
                }
                rawType = rawSuper;
            }
        }

        return toResolve;
    }

    private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
        Object declaration = typeVariable.getGenericDeclaration();
        return declaration instanceof Class<?> ? (Class<?>) declaration : null;
    }

    private static final class ParameterizedTypeImpl
        implements ParameterizedType {

        private final Type rawType;
        private final Type ownerType;
        private final Type[] args;

        ParameterizedTypeImpl(Type rawType, Type ownerType, Type[] args) {
            this.rawType = rawType;
            this.ownerType = ownerType;
            this.args = args.clone();
        }

        @Override
        public Type[] getActualTypeArguments() {
            return args.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ParameterizedType)) return false;
            ParameterizedType other = (ParameterizedType) obj;
            return Objects.equals(rawType, other.getRawType()) &&
            Objects.equals(ownerType, other.getOwnerType()) &&
            Arrays.equals(args, other.getActualTypeArguments());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(args) ^
            Objects.hashCode(rawType) ^
            Objects.hashCode(ownerType);
        }
    }

    private static final class GenericArrayTypeImpl implements GenericArrayType {

        private final Type componentType;

        GenericArrayTypeImpl(Type componentType) {
            this.componentType = componentType;
        }

        @Override
        public Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof GenericArrayType)) return false;
            GenericArrayType other = (GenericArrayType) obj;
            return Objects.equals(
                componentType,
                other.getGenericComponentType()
            );
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(componentType);
        }
    }

    private static final class WildcardTypeImpl implements WildcardType {

        private final Type[] upperBounds;
        private final Type[] lowerBounds;

        WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            this.upperBounds = upperBounds.clone();
            this.lowerBounds = lowerBounds.clone();
        }

        @Override
        public Type[] getUpperBounds() {
            return upperBounds.clone();
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBounds.clone();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof WildcardType)) return false;
            WildcardType other = (WildcardType) obj;
            return Arrays.equals(upperBounds, other.getUpperBounds()) &&
            Arrays.equals(lowerBounds, other.getLowerBounds());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(upperBounds) ^ Arrays.hashCode(lowerBounds);
        }
    }
}
