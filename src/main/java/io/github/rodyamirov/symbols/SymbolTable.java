package io.github.rodyamirov.symbols;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.rodyamirov.exceptions.TypeCheckException;
import io.github.rodyamirov.exceptions.VariableException;
import io.github.rodyamirov.lex.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by richard.rast on 12/27/16.
 */
public class SymbolTable {
    // lookup by scope, then look by token to see the registered type
    private final ImmutableMap<Scope, Map<Token<String>, TypeSpec>> symbolTable;

    private SymbolTable(ImmutableMap<Scope, Map<Token<String>, TypeSpec>> symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * Returns the set of all scopes which are known to be in this SymbolTable.
     * @return the set of all scopes which are known to be in this SymbolTable.
     */
    public ImmutableSet<Scope> knownScopes() {
        return this.symbolTable.keySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SymbolTable)) {
            return false;
        }

        SymbolTable other = (SymbolTable)o;
        return Objects.equals(this.symbolTable, other.symbolTable);
    }

    @Override
    public int hashCode() {
        return symbolTable.hashCode();
    }

    /**
     * Checks if there is a symbol defined by the specified token exactly at the specified
     * scope. So for example if there is a variable x defined at scope a.b, then
     * - isDefinedExactlyAt(a.b.c, x) would be false,
     * - isDefinedExactlyAt(a.b, x) would be true, and
     * - isDefinedExactlyAt(a, x) would be false.
     *
     * @param scope The exact scope to search at
     * @param idToken The name of the symbol to search for
     * @return True if there is a symbol by this name at or below the specified scope
     */
    public boolean isDefinedExactlyAt(Scope scope, Token idToken) {
        if (! symbolTable.containsKey(scope)) {
            return false;
        } else {
            Map<Token<String>, TypeSpec> localTable = symbolTable.get(scope);
            return localTable.containsKey(idToken);
        }
    }

    /**
     * Finds the uppermost scope, at or below the specified scope, where there is a registered
     * symbol matching the specified token.
     *
     * @param scope The top scope to look in
     * @param idToken The Token to match on
     * @return The Scope of the closest match for this token
     * @throws VariableException if there is no match at this scope or any scope below it
     */
    public Scope closestScopeFound(Scope scope, Token<String> idToken) {
        Scope originalScope = scope; // for error logging if needed

        boolean found = false;
        while (!found) {
            if (isDefinedExactlyAt(scope, idToken)) {
                found = true;
            } else if (scope.parentScope.isPresent()) {
                scope = scope.parentScope.get();
            } else {
                throw VariableException.notDefined(originalScope, idToken);
            }
        }

        return scope;
    }

    /**
     * Gets the type of the uppermost symbol exactly at the specified scope. So for example,
     * if there is a variable x:REAL at scope a and x:INTEGER at scope a.b (and nothing else
     * in the entire table), then
     * - getTypeExactlyAt(a.b.c, x) would be an exception,
     * - getTypeExactlyAt(a.b, x) would be INTEGER,
     * - getTypeExactlyAt(a, x) would be REAL, and
     * - getTypeExactlyAt(d, x) would be an exception.
     *
     * @param scope The uppermost scope to search from
     * @param idToken The name of the symbol to search for
     * @return The TypeSpec of the uppermost symbol with this id, at the specified scope
     * @throws VariableException if there is no symbol with this id at the specified scope
     */
    public TypeSpec getTypeExactlyAt(Scope scope, Token idToken) {
        if (!symbolTable.containsKey(scope)) {
            throw VariableException.notDefined(scope, idToken);
        } else {
            Map<Token<String>, TypeSpec> localTable = symbolTable.get(scope);
            if (localTable.containsKey(idToken)) {
                return localTable.get(idToken);
            } else {
                throw VariableException.notDefined(scope, idToken);
            }
        }
    }

    /**
     * Finds the uppermost symbol, at or below the specified scope, which matches the idToken.
     * Then returns the type associated to that variable at that scope.
     *
     * @param scope The scope to start matches at
     * @param idToken The token to match the symbol on
     * @throws VariableException if there is no matching symbol
     */
    public TypeSpec getType(Scope scope, Token idToken) {
        scope = closestScopeFound(scope, idToken);
        return getTypeExactlyAt(scope, idToken);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Scope, Map<Token<String>, TypeSpec>> toReturn;
        private boolean finished;

        private Builder() {
            toReturn = new HashMap<>();
            finished = false;
        }

        /**
         * Adds a specific token to the symbol table. Returns this for purpose of chaining.
         */
        public Builder addSymbol(Scope scope, Token<String> idToken, TypeSpec variableType) {
            Map<Token<String>, TypeSpec> localTable;

            if (! toReturn.containsKey(scope)) {
                localTable = new HashMap<>();
                toReturn.put(scope, localTable);
            } else {
                localTable = toReturn.get(scope);
            }

            if (localTable.containsKey(idToken)) {
                throw VariableException.doubleDefined(scope, idToken);
            } else {
                localTable.put(idToken, variableType);
                return this;
            }
        }

        /**
         * Builds the symbol table. To preserve immutability of the symbol table, this can only
         * be built once or it will throw an exception.
         *
         * @return The SymbolTable this Builder has been building.
         */
        public SymbolTable build() {
            if (finished) {
                throw new IllegalStateException("Symbol table has already been built!");
            } else {
                finished = true;
                return new SymbolTable(ImmutableMap.copyOf(toReturn));
            }
        }
    }
}
