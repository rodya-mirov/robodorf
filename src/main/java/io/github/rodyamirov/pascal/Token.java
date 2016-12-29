package io.github.rodyamirov.pascal;

import java.util.Objects;

/**
 * Created by richard.rast on 12/22/16.
 */
public class Token<T> {
    public enum Type {
        INT_CONSTANT, REAL_CONSTANT,

        VAR_TYPE,

        MINUS, PLUS, MOD,
        TIMES, REAL_DIVIDE, INT_DIVIDE,
        L_PAREN, R_PAREN,

        COMMA, COLON,

        PROGRAM,        // PROGRAM
        PROCEDURE,      // PROCEDURE
        VAR,            // VAR
        BEGIN,          // BEGIN
        END,            // END
        SEMI,           // ;
        DOT,            // .
        ASSIGN,         // :=
        ID,             // [_A-Za-z][A-Za-z0-9]*

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

    @Override
    public int hashCode() {
        int out = 137 * type.ordinal();
        if (value != null) {
            out += 13 * value.hashCode();
        }
        return out;
    }

    public static Token<Integer> INT_CONSTANT(int value) {
        return new Token<>(Type.INT_CONSTANT, value);
    }

    public static Token<Float> REAL_CONSTANT(float value) {
        return new Token<>(Type.REAL_CONSTANT, value);
    }

    public static Token<String> ID(String value) {
        value = Tokenizer.standardizeId(value);
        return new Token<>(Type.ID, value);
    }

    private static Token<Void> voidToken(Type type) {
        return new Token<>(type, null);
    }

    public static final Token<Void> EOF = voidToken(Type.EOF);

    public static final Token<Void> PLUS = voidToken(Type.PLUS);
    public static final Token<Void> MINUS = voidToken(Type.MINUS);
    public static final Token<Void> MOD = voidToken(Type.MOD);

    public static final Token<Void> TIMES = voidToken(Type.TIMES);
    public static final Token<Void> REAL_DIVIDE = voidToken(Type.REAL_DIVIDE);
    public static final Token<Void> INT_DIVIDE = voidToken(Type.INT_DIVIDE);

    public static final Token<Void> L_PAREN = voidToken(Type.L_PAREN);
    public static final Token<Void> R_PAREN = voidToken(Type.R_PAREN);

    public static final Token<Void> SEMI = voidToken(Type.SEMI);
    public static final Token<Void> DOT = voidToken(Type.DOT);
    public static final Token<Void> COMMA = voidToken(Type.COMMA);
    public static final Token<Void> COLON = voidToken(Type.COLON);

    public static final Token<Void> ASSIGN = voidToken(Type.ASSIGN);

    public static final Token<Void> BEGIN = voidToken(Type.BEGIN);
    public static final Token<Void> END = voidToken(Type.END);
    public static final Token<Void> PROGRAM = voidToken(Type.PROGRAM);
    public static final Token<Void> PROCEDURE = voidToken(Type.PROCEDURE);
    public static final Token<Void> VAR = voidToken(Type.VAR);

    public static final Token<TypeSpec> INT_TYPE = new Token<>(Type.VAR_TYPE, TypeSpec.INTEGER);
    public static final Token<TypeSpec> REAL_TYPE = new Token<>(Type.VAR_TYPE, TypeSpec.REAL);
}
