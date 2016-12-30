package io.github.rodyamirov.symbols;

import io.github.rodyamirov.lex.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by richard.rast on 12/27/16.
 */
public class SymbolTable {
    private final Map<Token<String>, TypeSpec> symbolTable;

    private SymbolTable() {
        symbolTable = new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SymbolTable)) {
            return false;
        }

        SymbolTable other = (SymbolTable)o;
        return Objects.equals(this.symbolTable, other.symbolTable);
    }

    public static SymbolTable empty() {
        return new SymbolTable();
    }

    public boolean isDefined(Token idToken) {
        return symbolTable.containsKey(idToken);
    }

    public TypeSpec getType(Token idToken) {
        if (symbolTable.containsKey(idToken)) {
            return symbolTable.get(idToken);
        } else {
            String message = String.format("Unrecognized variable token: %s", idToken.toString());
            throw new VariableException(message);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SymbolTable toReturn;
        private boolean finished;

        private Builder() {
            toReturn = new SymbolTable();
            finished = false;
        }

        /**
         * Adds a specific token to the symbol table. Returns this for purpose of chaining.
         */
        public Builder addSymbol(Token<String> idToken, TypeSpec variableType) {
            toReturn.symbolTable.put(idToken, variableType);
            return this;
        }

        public SymbolTable build() {
            if (finished) {
                throw new IllegalStateException("Symbol table has already been built!");
            } else {
                finished = true;
                return toReturn;
            }
        }
    }
}
