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

    /**
     * Attempt to convert a symbolvalue to another type. This returns the input if it's
     * already in the correct type. The only other acceptable conversion (at this time)
     * is from int to float, which does the obvious thing.
     *
     * @param from The SymbolValue to be converted
     * @param desiredType The type we want to convert it to
     * @return A SymbolValue with the correct type and an equivalent value
     * @throws TypeCheckException if there is no conversion possible
     */
    public static SymbolValue convert(SymbolValue from, TypeSpec desiredType) {
        if (from.typeSpec == desiredType) {
            return from;
        } else {
            switch (desiredType) {
                case INTEGER: // no nontrivial conversions possible
                    break;

                case REAL:
                    if (from.typeSpec == TypeSpec.INTEGER) {
                        return SymbolValue.make(desiredType, ((Integer) from.value).floatValue());
                    }

                default:
                    break;
            }
        }

        throw TypeCheckException.conversionImpossible(from.typeSpec, desiredType);
    }

    /**
     * A helper method for determining if the output value should be an int or a float.
     * This is used for +, -, and *; essentially if one of the inputs is a float, then
     * the output should also be a float. If they're both ints, the output should be an
     * int.
     *
     * Note that this is not used for div or mod (which require both to be integers) or
     * / (which outputs a float regardless).
     *
     * @param a The type of the left argument
     * @param b The type of the right argument
     * @return The type of the output
     * @throws TypeCheckException if there is no valid return type for the input
     */
    private static TypeSpec arithIntOrFloat(TypeSpec a, TypeSpec b) {
        if (a == TypeSpec.INTEGER && b == TypeSpec.INTEGER) {
            return TypeSpec.INTEGER;
        } else if (a == TypeSpec.INTEGER && b == TypeSpec.REAL) {
            return TypeSpec.REAL;
        } else if (a == TypeSpec.REAL && b == TypeSpec.INTEGER) {
            return TypeSpec.REAL;
        } else if (a == TypeSpec.REAL && b == TypeSpec.REAL) {
            return TypeSpec.REAL;
        } else {
            String message = String.format(
                    "Cannot find a common arithmetic type between %s and %s",
                    a.name(), b.name()
            );
            throw new TypeCheckException(message);
        }
    }

    /**
     * Converts the specified SymbolValue to REAL then returns its value as a float.
     * @param a The SymbolValue to be converted
     * @return A float representing the symbol value
     * @throws TypeCheckException if <code>a</code> cannot be converted to a REAL
     */
    private static float toReal(SymbolValue a) {
        SymbolValue<Float> converted = convert(a, TypeSpec.REAL);
        return converted.value;
    }

    /**
     * Converts the SymbolValue to INTEGER then returns its value as an int.
     * @param a The SymbolValue to be converted
     * @return A float representing the symbol value
     * @throws TypeCheckException if <code>a</code> cannot be converted to an INTEGER
     */
    private static int toInt(SymbolValue a) {
        SymbolValue<Integer> converted = convert(a, TypeSpec.INTEGER);
        return converted.value;
    }

    public static SymbolValue add(SymbolValue a, SymbolValue b) {
        TypeSpec out = arithIntOrFloat(a.typeSpec, b.typeSpec);

        switch (out) {
            case INTEGER:
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
        TypeSpec out = arithIntOrFloat(a.typeSpec, b.typeSpec);

        switch (out) {
            case INTEGER:
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
        TypeSpec out = arithIntOrFloat(a.typeSpec, b.typeSpec);

        switch (out) {
            case INTEGER:
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
        return new SymbolValue<>(TypeSpec.INTEGER, toInt(a) / toInt(b));
    }

    public static SymbolValue<Integer> intMod(SymbolValue a, SymbolValue b) {
        return new SymbolValue<>(TypeSpec.INTEGER, toInt(a) % toInt(b));
    }

    public static SymbolValue<Float> realDivide(SymbolValue a, SymbolValue b) {
        return new SymbolValue<>(TypeSpec.REAL, toReal(a) / toReal(b));
    }

    public static SymbolValue neg(SymbolValue a) {
        TypeSpec out = a.typeSpec;

        switch (out) {
            case INTEGER:
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
            case INTEGER:
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
