package io.github.rodyamirov.symbols;

import io.github.rodyamirov.exceptions.TypeCheckException;
import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.tree.BlockNode;
import io.github.rodyamirov.tree.CompoundNode;
import io.github.rodyamirov.tree.DeclarationNode;
import io.github.rodyamirov.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.tree.ProgramNode;
import io.github.rodyamirov.utils.Procedure;
import org.junit.Test;

import static io.github.rodyamirov.symbols.ScopeAssigner.ROOT_SCOPE;
import static io.github.rodyamirov.utils.ListHelper.list;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by richard.rast on 12/30/16.
 */
public class SymbolValueTest {
    private void checkException(Procedure procedure, Class exceptionClass, String errorMessage) {
        try {
            procedure.invoke();
            assertThat("This shouldn't happen", true, is(false));
        } catch (Exception thrown) {
            assertThat("Class is correct", thrown, isA(exceptionClass));
            assertThat("Message is correct", thrown.getMessage(), is(errorMessage));
        }
    }

    @Test
    public void boolTest() {
        TypeSpec typeSpec;
        SymbolValue symbolValue, symbolValue2;

        typeSpec = TypeSpec.BOOLEAN;

        symbolValue = SymbolValue.make(typeSpec, true);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(true));

        symbolValue2 = SymbolValue.make(typeSpec, true);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, false);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        symbolValue = SymbolValue.make(typeSpec, false);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(false));

        symbolValue2 = SymbolValue.make(typeSpec, false);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, true);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        checkException(
                () -> SymbolValue.make(typeSpec, null),
                TypeCheckException.class,
                "Cannot assign a value of null for type BOOLEAN"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.INTEGER, true),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Boolean, but required a value of type class java.lang.Integer"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.BOOLEAN, 1317),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Integer, but required a value of type class java.lang.Boolean"
        );
    }

    @Test
    public void intTest() {
        TypeSpec typeSpec;
        SymbolValue symbolValue, symbolValue2;

        typeSpec = TypeSpec.INTEGER;

        symbolValue = SymbolValue.make(typeSpec, 12);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(12));

        symbolValue2 = SymbolValue.make(typeSpec, 12);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, 15);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        symbolValue = SymbolValue.make(typeSpec, -611);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(-611));

        symbolValue2 = SymbolValue.make(typeSpec, -611);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, 13480);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        checkException(
                () -> SymbolValue.make(typeSpec, null),
                TypeCheckException.class,
                "Cannot assign a value of null for type INTEGER"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.INTEGER, true),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Boolean, but required a value of type class java.lang.Integer"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.BOOLEAN, 1317),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Integer, but required a value of type class java.lang.Boolean"
        );
    }

    @Test
    public void realTest() {
        TypeSpec typeSpec;
        SymbolValue symbolValue, symbolValue2;

        typeSpec = TypeSpec.REAL;

        symbolValue = SymbolValue.make(typeSpec, 12.0f);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(12.0f));

        symbolValue2 = SymbolValue.make(typeSpec, 12.0f);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, 15.0f);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        symbolValue = SymbolValue.make(typeSpec, -611.0f);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(-611.0f));

        symbolValue2 = SymbolValue.make(typeSpec, -611.0f);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, 13480.0f);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        checkException(
                () -> SymbolValue.make(typeSpec, null),
                TypeCheckException.class,
                "Cannot assign a value of null for type REAL"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.REAL, 12),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Integer, but required a value of type class java.lang.Float"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.INTEGER, 1317.0f),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Float, but required a value of type class java.lang.Integer"
        );
    }

    @Test
    public void programTest() {
        TypeSpec typeSpec = TypeSpec.PROGRAM;
        Token<String> programName = Token.ID("program1");
        Token<String> programName3 = Token.ID("program3");

        ProgramNode programNode1 = new ProgramNode(
                programName,
                new BlockNode(
                        new DeclarationNode(list(), list()),
                        new CompoundNode(list())
                )
        );
        ScopeAssigner.assignScopes(ROOT_SCOPE, programNode1);

        ProgramNode programNode2 = new ProgramNode(
                programName,
                new BlockNode(
                        new DeclarationNode(list(), list()),
                        new CompoundNode(list())
                )
        );
        ScopeAssigner.assignScopes(ROOT_SCOPE, programNode2);

        ProgramNode programNode3 = new ProgramNode(
                programName3,
                new BlockNode(
                        new DeclarationNode(list(), list()),
                        new CompoundNode(list())
                )
        );
        ScopeAssigner.assignScopes(ROOT_SCOPE, programNode3);

        SymbolValue symbolValue, symbolValue2;

        symbolValue = SymbolValue.make(typeSpec, programNode1);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(programNode1));

        symbolValue2 = SymbolValue.make(typeSpec, programNode2);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, programNode3);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        symbolValue = SymbolValue.make(typeSpec, programNode3);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(programNode3));

        checkException(
                () -> SymbolValue.make(typeSpec, null),
                TypeCheckException.class,
                "Cannot assign a value of null for type PROGRAM"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.REAL, programNode2),
                TypeCheckException.class,
                "Attempted to assign a value of type class io.github.rodyamirov.tree.ProgramNode, but required a value of type class java.lang.Float"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.PROGRAM, 1317.0f),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Float, but required a value of type class io.github.rodyamirov.tree.ProgramNode"
        );
    }

    @Test
    public void procedureTest() {
        TypeSpec typeSpec = TypeSpec.PROCEDURE;
        Token<String> procedureName = Token.ID("program1");
        Token<String> procedureName3 = Token.ID("program3");

        ProcedureDeclarationNode proc1 = new ProcedureDeclarationNode(
                procedureName,
                new BlockNode(
                        new DeclarationNode(list(), list()),
                        new CompoundNode(list())
                )
        );
        ScopeAssigner.assignScopes(ROOT_SCOPE, proc1);

        ProcedureDeclarationNode proc2 = new ProcedureDeclarationNode(
                procedureName,
                new BlockNode(
                        new DeclarationNode(list(), list()),
                        new CompoundNode(list())
                )
        );
        ScopeAssigner.assignScopes(ROOT_SCOPE, proc2);

        ProcedureDeclarationNode proc3 = new ProcedureDeclarationNode(
                procedureName3,
                new BlockNode(
                        new DeclarationNode(list(), list()),
                        new CompoundNode(list())
                )
        );
        ScopeAssigner.assignScopes(ROOT_SCOPE, proc3);

        SymbolValue symbolValue, symbolValue2;

        symbolValue = SymbolValue.make(typeSpec, proc1);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(proc1));

        symbolValue2 = SymbolValue.make(typeSpec, proc2);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, proc3);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        symbolValue = SymbolValue.make(typeSpec, proc3);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(proc3));

        checkException(
                () -> SymbolValue.make(typeSpec, null),
                TypeCheckException.class,
                "Cannot assign a value of null for type PROCEDURE"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.REAL, proc2),
                TypeCheckException.class,
                "Attempted to assign a value of type class io.github.rodyamirov.tree.ProcedureDeclarationNode, but required a value of type class java.lang.Float"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.PROCEDURE, 1317.0f),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Float, but required a value of type class io.github.rodyamirov.tree.ProcedureDeclarationNode"
        );
    }
}
