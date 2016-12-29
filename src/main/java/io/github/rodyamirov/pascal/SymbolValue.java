package io.github.rodyamirov.pascal;

import io.github.rodyamirov.pascal.visitor.TypeCheckException;

import java.util.Objects;

/**
 * Created by richard.rast on 12/27/16.
 */
public class SymbolValue<T> {
    public final TypeSpec typeSpec;
    public final T value;

    private SymbolValue(TypeSpec typeSpec, T value) {
        this.typeSpec = typeSpec;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SymbolValue)) {
            return false;
        }

        SymbolValue other = (SymbolValue)o;

        return Objects.equals(this.typeSpec, other.typeSpec)
                && Objects.equals(this.value, other.value);
    }

    public static SymbolValue make(TypeSpec typeSpec, Object value) {
        Class desiredClass = typeSpec.getValueClass();

        if (desiredClass.isInstance(value)) {
            return new SymbolValue<>(typeSpec, desiredClass.cast(value));
        } else {
            throw TypeCheckException.wrongValueClass(value.getClass(), desiredClass);
        }
    }

    public static SymbolValue convert(SymbolValue from, TypeSpec desiredType) {
        if (from.typeSpec == desiredType) {
            return from;
        }

        switch (desiredType) {
            case INT: // no nontrivial conversions possible
                break;

            case REAL:
                if (from.typeSpec == TypeSpec.INT) {
                    return SymbolValue.make(desiredType, ((Integer) from.value).floatValue());
                }

            default:
                break;
        }

        throw TypeCheckException.conversionImpossible(from.typeSpec, desiredType);
    }

    private static TypeSpec arithTypeSpec(TypeSpec a, TypeSpec b) {
        if (a == TypeSpec.INT && b == TypeSpec.INT) {
            return TypeSpec.INT;
        } else if (a == TypeSpec.INT && b == TypeSpec.REAL) {
            return TypeSpec.REAL;
        } else if (a == TypeSpec.REAL && b == TypeSpec.INT) {
            return TypeSpec.REAL;
        } else if (a == TypeSpec.REAL && b == TypeSpec.REAL) {
            return TypeSpec.REAL;
        } else {
            String message = String.format(
                    "Cannot find a common arithmetic type between %s and %s",
                    a.name(), b.name()
            );
            throw new IllegalArgumentException(message);
        }
    }

    private static float toReal(SymbolValue a) {
        switch (a.typeSpec) {
            case INT:
                return ((Integer) a.value).floatValue();

            case REAL:
                return (Float) a.value;

            default:
                throw TypeCheckException.wrongValueClass(a.typeSpec, TypeSpec.REAL);
        }
    }

    private static int toInt(SymbolValue a) {
        switch (a.typeSpec) {
            case INT:
                return (Integer) a.value;

            default:
                throw TypeCheckException.wrongValueClass(a.typeSpec, TypeSpec.INT);
        }
    }

    public static SymbolValue add(SymbolValue a, SymbolValue b) {
        TypeSpec out = arithTypeSpec(a.typeSpec, b.typeSpec);

        switch (out) {
            case INT:
                return new SymbolValue<>(out, toInt(a) + toInt(b));

            case REAL:
                return new SymbolValue<>(out, toReal(a) + toReal(b));

            default:
                String message = String.format(
                        "Cannot add two values to get type %s", out.name()
                );
                throw new IllegalArgumentException(message);
        }
    }

    public static SymbolValue subtract(SymbolValue a, SymbolValue b) {
        TypeSpec out = arithTypeSpec(a.typeSpec, b.typeSpec);

        switch (out) {
            case INT:
                return new SymbolValue<>(out, toInt(a) - toInt(b));

            case REAL:
                return new SymbolValue<>(out, toReal(a) - toReal(b));

            default:
                String message = String.format(
                        "Cannot subtract two values to get type %s", out.name()
                );
                throw new IllegalArgumentException(message);
        }
    }

    public static SymbolValue multiply(SymbolValue a, SymbolValue b) {
        TypeSpec out = arithTypeSpec(a.typeSpec, b.typeSpec);

        switch (out) {
            case INT:
                return new SymbolValue<>(out, toInt(a) * toInt(b));

            case REAL:
                return new SymbolValue<>(out, toReal(a) * toReal(b));

            default:
                String message = String.format(
                        "Cannot multiply two values to get type %s", out.name()
                );
                throw new IllegalArgumentException(message);
        }
    }

    public static SymbolValue<Integer> intDivide(SymbolValue a, SymbolValue b) {
        return new SymbolValue<>(TypeSpec.INT, toInt(a) / toInt(b));
    }

    public static SymbolValue<Float> realDivide(SymbolValue a, SymbolValue b) {
        return new SymbolValue<>(TypeSpec.REAL, toReal(a) / toReal(b));
    }

    public static SymbolValue neg(SymbolValue a) {
        TypeSpec out = a.typeSpec;

        switch (out) {
            case INT:
                return new SymbolValue<>(out, -toInt(a));

            case REAL:
                return new SymbolValue<>(out, -toReal(a));

            default:
                String message = String.format(
                        "Cannot negate a value of type %s", out.name()
                );
                throw new IllegalArgumentException(message);
        }
    }

    public static SymbolValue pos(SymbolValue a) {
        TypeSpec out = a.typeSpec;

        switch (out) {
            case INT:
                return new SymbolValue<>(out, toInt(a));

            case REAL:
                return new SymbolValue<>(out, toReal(a));

            default:
                String message = String.format(
                        "Cannot enact 'pos' on type %s", out.name()
                );
                throw new IllegalArgumentException(message);
        }
    }
}
