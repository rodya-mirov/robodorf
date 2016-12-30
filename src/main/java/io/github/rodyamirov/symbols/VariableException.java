package io.github.rodyamirov.symbols;

import io.github.rodyamirov.lex.Token;

/**
 * Special error code indicating a problem has happened when parsing a variable name.
 * This would often be thrown when a variable's value is used before it is assigned!
 * Created by richard.rast on 12/25/16.
 */
public class VariableException extends IllegalStateException {
    public VariableException(String message) {
        super(message);
    }

    public static VariableException notAssigned(Scope scope, Token idToken) {
        String errorMessage = String.format(
                "The variable %s : %s does not have an assigned value!",
                scope.toString(), idToken.value.toString()
        );
        return new VariableException(errorMessage);
    }

    public static VariableException notDefined(Scope scope, Token idToken) {
        String errorMessage = String.format(
                "The variable %s : %s has not been declared!",
                scope.toString(), idToken.value.toString()
        );
        return new VariableException(errorMessage);
    }
}
