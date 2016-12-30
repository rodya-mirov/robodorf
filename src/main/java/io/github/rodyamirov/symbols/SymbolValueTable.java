package io.github.rodyamirov.symbols;

import io.github.rodyamirov.exceptions.TypeCheckException;
import io.github.rodyamirov.exceptions.VariableException;
import io.github.rodyamirov.lex.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by richard.rast on 12/27/16.
 */
public class SymbolValueTable {
    private final SymbolTable symbolTable;
    private final Map<Scope, Map<Token<String>, SymbolValue>> valueTables;

    public SymbolValueTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        valueTables = new HashMap<>();

        for (Scope scope : symbolTable.knownScopes()) {
            valueTables.put(scope, new HashMap<>());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SymbolValueTable)) {
            return false;
        }

        SymbolValueTable other = (SymbolValueTable)o;

        return Objects.equals(this.symbolTable, other.symbolTable)
                && Objects.equals(this.valueTables, other.valueTables);
    }

    /**
     * Determines if there is a symbol matching the specified token at or below the specified scope.
     * So if the table has (only) x defined at a.b and y defined at a, then
     * - isDefined(a.b.c, x) is true
     * - isDefined(a.b.c, y) is true
     * - isDefined(a.b, x) is true
     * - isDefined(a.b, y) is true
     * - isDefined(a, x) is false
     * - isDefined(a, y) is true
     *
     * @param scope The uppermost scope to look for registered symbols
     * @param idToken the name of the symbol to search for
     * @return true iff there is a symbol registered by this name, at or above the specified scope
     */
    public boolean isDefined(Scope scope, Token<String> idToken) {
        Supplier<Boolean> checkParentScope =
                () -> scope.parentScope
                        .map(ps -> isDefined(ps, idToken))
                        .orElse(false);

        if (symbolTable.isDefinedExactlyAt(scope, idToken)) {
            return true;
        } else {
            return checkParentScope.get();
        }
    }

    /**
     * Determines if the variable most closely defined at the current scope has been initialized.
     * Suppose the table contains x at a (not initialized) and x at a.b (is ininitialized). Then:
     * - isInitialized(a.b.c, x) is true
     * - isInitialized(a.b, x) is true
     * - isInitialized(a, x) is false
     * - isInitialized(a.b, y) gives an exception
     *
     * @param scope The uppermost scope to look for registered symbols
     * @param idToken the name of the symbol to search for
     * @return true iff the closest match for this symbol has been initialized
     * @throws VariableException if there is no matching symbol
     */
    public boolean isInitialized(Scope scope, Token<String> idToken) {
        scope = closestScopeFound(scope, idToken);

        // valueTables.get(scope) is guaranteed to exist, as we created it in the constructor
        return valueTables.get(scope).containsKey(idToken);
    }

    /**
     * Gets the value stored for the uppermost symbol at or below the specified scope, which matches
     * the specified token.
     * @param scope The top scope to look at
     * @param idToken The token to match the symbol on
     * @return The value associated with the matching scope
     * @throws VariableException if there is no matching symbol
     * @throws VariableException if the matching symbol has not been initialized
     */
    public SymbolValue getValue(Scope scope, Token<String> idToken) {
        scope = closestScopeFound(scope, idToken);

        if (isInitialized(scope, idToken)) {
            return valueTables.get(scope).get(idToken);
        } else {
            throw VariableException.notAssigned(scope, idToken);
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
            if (symbolTable.isDefinedExactlyAt(scope, idToken)) {
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
     * Finds the uppermost symbol, at or below the specified scope, which matches the idToken.
     * It then sets the associated value to the specified value.
     *
     * @param scope The scope to start matches at
     * @param idToken The token to match the symbol on
     * @param value The value to assign to the symbol
     * @throws VariableException if there is no matching symbol
     * @throws TypeCheckException if the specified value cannot be converted to the required value
     */
    public void setValue(Scope scope, Token<String> idToken, SymbolValue value) {
        scope = closestScopeFound(scope, idToken);

        TypeSpec desired = symbolTable.getTypeExactlyAt(scope, idToken);
        SymbolValue setValue = SymbolValueOps.convert(value, desired);

        valueTables.get(scope).put(idToken, setValue);
    }
}
