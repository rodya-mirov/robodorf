package io.github.rodyamirov.symbols;

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

        if (value == null) {
            if (typeSpec.acceptsNullValues()){
                return new SymbolValue(typeSpec, null);
            } else {
                throw TypeCheckException.nullNotAllowed(typeSpec);
            }
        } else if (desiredClass.isInstance(value)) {
            return new SymbolValue<>(typeSpec, desiredClass.cast(value));
        } else {
            throw TypeCheckException.wrongValueClass(value.getClass(), desiredClass);
        }
    }
}
