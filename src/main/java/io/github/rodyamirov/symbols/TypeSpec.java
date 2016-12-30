package io.github.rodyamirov.symbols;

import io.github.rodyamirov.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.tree.ProgramNode;

/**
 * Created by richard.rast on 12/26/16.
 */
public enum TypeSpec {
    REAL, INTEGER, BOOLEAN, // datatypes

    PROGRAM, PROCEDURE;     // the words get reserved but they don't mean the same as they used to

    public boolean acceptsNullValues() {
        return acceptsNullValues(this);
    }

    public Class getValueClass() {
        return getValueClass(this);
    }

    public static boolean acceptsNullValues(TypeSpec typeSpec) {
        switch (typeSpec) {
            case INTEGER:
            case BOOLEAN:
            case REAL:
            case PROCEDURE:
            case PROGRAM:
                return false;

            default:
                String errorMessage = String.format(
                        "Unspecified behavior for TypeSpec %s",
                        typeSpec.name()
                );
                throw new IllegalArgumentException(errorMessage);
        }
    }

    public static Class getValueClass(TypeSpec typeSpec) {
        switch (typeSpec) {
            case INTEGER: return Integer.class;
            case REAL: return Float.class;
            case BOOLEAN: return Boolean.class;

            case PROCEDURE: return ProcedureDeclarationNode.class;
            case PROGRAM: return ProgramNode.class;

            default:
                String errorMessage = String.format(
                        "No value type associated with TypeSpec %s",
                        typeSpec.name()
                );
                throw new IllegalArgumentException(errorMessage);
        }
    }

    public boolean isInstance(Object value) {
        return getValueClass().isInstance(value);
    }
}
