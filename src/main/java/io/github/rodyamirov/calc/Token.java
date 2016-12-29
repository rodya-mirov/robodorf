package io.github.rodyamirov.calc;

import java.util.Objects;

/**
 * Created by richard.rast on 12/22/16.
 */
public class Token<T> {
    public enum Type {
        INT,
        MINUS, PLUS,
        TIMES, DIVIDE,
        L_PAREN, R_PAREN,
        EOF
    }

    public final Type type;
    public final T value;

    private Token(Type type, T value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Token { TYPE: %s, VALUE: %s }", type, value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Token)) {
            return false;
        }

        Token other = (Token) o;
        return this.type == other.type
                && Objects.equals(this.value, other.value);
    }

    public static Token<Integer> INT(int value) {
        return new Token<>(Type.INT, value);
    }

    private static Token<Void> voidToken(Type type) {
        return new Token<>(type, null);
    }

    public static final Token<Void> EOF = voidToken(Type.EOF);

    public static final Token<Void> PLUS = voidToken(Type.PLUS);
    public static final Token<Void> MINUS = voidToken(Type.MINUS);

    public static final Token<Void> TIMES = voidToken(Type.TIMES);
    public static final Token<Void> DIVIDE = voidToken(Type.DIVIDE);

    public static final Token<Void> L_PAREN = voidToken(Type.L_PAREN);
    public static final Token<Void> R_PAREN = voidToken(Type.R_PAREN);
}
