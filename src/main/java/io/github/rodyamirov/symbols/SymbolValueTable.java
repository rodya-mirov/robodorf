package io.github.rodyamirov.symbols;

import io.github.rodyamirov.lex.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by richard.rast on 12/27/16.
 */
public class SymbolValueTable {
    private final SymbolTable symbolTable;
    private final Map<Token<String>, SymbolValue> values;

    public SymbolValueTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        values = new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SymbolValueTable)) {
            return false;
        }

        SymbolValueTable other = (SymbolValueTable)o;

        return Objects.equals(this.symbolTable, other.symbolTable)
                && Objects.equals(this.values, other.values);
    }

    public boolean isDefined(Token<String> idToken) {
        return symbolTable.isDefined(idToken);
    }

    public boolean isInitialized(Token<String> idToken) {
        return symbolTable.isDefined(idToken) && values.containsKey(idToken);
    }

    public SymbolValue getValue(Token<String> idToken) {
        if (isInitialized(idToken)) {
            return values.get(idToken);
        } else {
            throw VariableException.notAssigned(idToken);
        }
    }

    public void setValue(Token<String> idToken, SymbolValue value) {
        if (! isDefined(idToken)) {
            throw VariableException.notDefined(idToken);
        }

        TypeSpec desired = symbolTable.getType(idToken);
        SymbolValue setValue = SymbolValueOps.convert(value, desired);

        values.put(idToken, setValue);
    }
}
