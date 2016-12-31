package io.github.rodyamirov.exceptions;

import io.github.rodyamirov.lex.Token;

/**
 * Created by richard.rast on 12/30/16.
 */
public class UnexpectedTokenException extends IllegalStateException {
    public UnexpectedTokenException(String message) {
        super(message);
    }

    public static UnexpectedTokenException wrongType(Token.Type type) {
        String message = String.format("Unexpected token type %s", type.name());
        return new UnexpectedTokenException(message);
    }
}
