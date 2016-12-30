package io.github.rodyamirov.symbols;

/**
 * Created by richard.rast on 12/26/16.
 */
public enum TypeSpec {
    REAL, INTEGER, BOOLEAN;

    public boolean acceptsNullValues() {
        return acceptsNullValues(this);
    }

    public Class getValueClass() {
        return getValueClass(this);
    }

    public static boolean acceptsNullValues(TypeSpec typeSpec) {
        switch (typeSpec) {
            case INTEGER:
            case BOOLEAN:
            case REAL:
                return false;

            default:
                String errorMessage = String.format(
                        "Unspecified behavior for TypeSpec %s",
                        typeSpec.name()
                );
                throw new IllegalArgumentException(errorMessage);
        }
    }

    public static Class getValueClass(TypeSpec typeSpec) {
        switch (typeSpec) {
            case INTEGER: return Integer.class;
            case REAL: return Float.class;
            case BOOLEAN: return Boolean.class;

            default:
                String errorMessage = String.format(
                        "No value type associated with TypeSpec %s",
                        typeSpec.name()
                );
                throw new IllegalArgumentException(errorMessage);
        }
    }

    public boolean isInstance(Object value) {
        return getValueClass().isInstance(value);
    }
}
