package io.github.rodyamirov.symbols;

import io.github.rodyamirov.exceptions.VariableException;
import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.parse.Parser;
import io.github.rodyamirov.tree.ProgramNode;
import org.junit.Test;

import static io.github.rodyamirov.parse.Parser.ROOT_SCOPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/30/16.
 */
public class SymbolTableBuilderTest {
    private void doBuildProgramTest(String text, SymbolTable desired) {
        ProgramNode programNode = Parser.parseProgram(text);
        SymbolTable actual = SymbolTableBuilder.buildFrom(programNode);
        assertThat("Symbol Table is correct", actual, is(desired));
    }

    @Test
    public void programTest1() {
        Token<String> progName = Token.ID("test1");
        String progText = "program test1; begin end.";
        SymbolTable desired = SymbolTable.builder()
                .addSymbol(ROOT_SCOPE, progName, TypeSpec.PROGRAM)
                .build();
        doBuildProgramTest(progText, desired);
    }

    @Test
    public void programTest2() {
        Token<String> progName = Token.ID("test2");
        Scope programScope = ROOT_SCOPE.makeChildScope(progName);
        String progText = "program test2; var a, b: integer; c: REAL; d: BOOLEAN; begin end.";
        SymbolTable desired = SymbolTable.builder()
                .addSymbol(ROOT_SCOPE, progName, TypeSpec.PROGRAM)
                .addSymbol(programScope, Token.ID("a"), TypeSpec.INTEGER)
                .addSymbol(programScope, Token.ID("b"), TypeSpec.INTEGER)
                .addSymbol(programScope, Token.ID("c"), TypeSpec.REAL)
                .addSymbol(programScope, Token.ID("d"), TypeSpec.BOOLEAN)
                .build();
        doBuildProgramTest(progText, desired);
    }

    @Test
    public void programTest3() {
        Token<String> progName = Token.ID("test3");
        Token<String> procName1 = Token.ID("proc1");
        Token<String> procName2 = Token.ID("proc2");
        Token<String> procName3 = Token.ID("proc3");

        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        Scope proc1Scope = progScope.makeChildScope(procName1);
        Scope proc2Scope = progScope.makeChildScope(procName2);
        Scope proc3Scope = proc2Scope.makeChildScope(procName3);

        String progText = ""
                + "program test3;"
                + "     var a, b: Integer;"
                + "procedure proc1;"
                + "     var b, c: Real;"
                + "     BEGIN"
                + "         b := a + 1;"    // not really important but meh, give em something
                + "         a := 12"
                + "     END;"               // semicolon is necessary
                + "procedure proc2;"
                + "     var a: Boolean;"
                + "         d: INTEGER;"
                + "     procedure proc3;"
                + "         var d: REAL;"
                + "             e: Integer;"
                + "         begin {proc3}"
                + "             while 1<2 do ;"                 // empty while loop just for fun
                + "             d := 12;"
                + "             while d > 5 do d := d-1;"       // actually does something
                + "             do break until 1<2;"            // this is pretty stupid i admit
                + "             do continue until 1=2;"         // infinite loop!
                + "             for e:=d to d+12 do d:=2;"
                + "             for e:=d downto d-12 do d:=2"
                + "         end {proc3};"
                + "     begin {proc2}"
                + "         d := 12;"
                + "         do a := not a until a;"
                + "         proc3();" // semi unnecessary, un-harmful
                + "     end {proc2};"
                + "begin {test3 -- the actual program!}"
                + "     a := 12;"
                + "     b := a+1;"
                + "     proc1();"
                + "     proc2()"
                + "end {test3} .";
        SymbolTable symbolTable = SymbolTable.builder()
                .addSymbol(ROOT_SCOPE, progName, TypeSpec.PROGRAM)
                .addSymbol(progScope, Token.ID("a"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.INTEGER)

                .addSymbol(progScope, procName1, TypeSpec.PROCEDURE)
                .addSymbol(proc1Scope, Token.ID("b"), TypeSpec.REAL)
                .addSymbol(proc1Scope, Token.ID("c"), TypeSpec.REAL)

                .addSymbol(progScope, procName2, TypeSpec.PROCEDURE)
                .addSymbol(proc2Scope, Token.ID("a"), TypeSpec.BOOLEAN)
                .addSymbol(proc2Scope, Token.ID("d"), TypeSpec.INTEGER)

                .addSymbol(proc2Scope, procName3, TypeSpec.PROCEDURE)
                .addSymbol(proc3Scope, Token.ID("d"), TypeSpec.REAL)
                .addSymbol(proc3Scope, Token.ID("e"), TypeSpec.INTEGER)

                .build();

        doBuildProgramTest(progText, symbolTable);
    }

    @Test
    public void doubleDefineErrorTest1() {
        String progText = "program test; var a, a: Integer; begin end.";

        // error is not thrown when parsing ...
        ProgramNode programNode = Parser.parseProgram(progText);

        try {
            SymbolTableBuilder.buildFrom(programNode);
            assertThat("Shouldn't be here", true, is(false));
        } catch (VariableException ve) {
            String errorMessage = "The token a already has a definition in the scope root.test!";
            assertThat(ve.getMessage(), is(errorMessage));
        }
    }

    @Test
    public void doubleDefineErrorTest2() {
        String progText = "program test; var a: Integer; a: Integer; begin end.";

        // error is not thrown when parsing ...
        ProgramNode programNode = Parser.parseProgram(progText);

        try {
            SymbolTableBuilder.buildFrom(programNode);
            assertThat("Shouldn't be here", true, is(false));
        } catch (VariableException ve) {
            String errorMessage = "The token a already has a definition in the scope root.test!";
            assertThat(ve.getMessage(), is(errorMessage));
        }
    }

    @Test
    public void doubleDefineErrorTest3() {
        String progText = "program test; var a: Integer; a: BOOLEAN; begin end.";

        // error is not thrown when parsing ...
        ProgramNode programNode = Parser.parseProgram(progText);

        try {
            SymbolTableBuilder.buildFrom(programNode);
            assertThat("Shouldn't be here", true, is(false));
        } catch (VariableException ve) {
            String errorMessage = "The token a already has a definition in the scope root.test!";
            assertThat(ve.getMessage(), is(errorMessage));
        }
    }

    @Test
    public void doubleDefineErrorTest4() {
        String progText = "program test; var a: Integer;"
                + "procedure p1; var a: REAL; a: BOOLEAN; begin end;"
                + " begin end.";

        // error is not thrown when parsing ...
        ProgramNode programNode = Parser.parseProgram(progText);

        try {
            SymbolTableBuilder.buildFrom(programNode);
            assertThat("Shouldn't be here", true, is(false));
        } catch (VariableException ve) {
            String errorMessage = "The token a already has a definition in the scope root.test.p1!";
            assertThat(ve.getMessage(), is(errorMessage));
        }
    }
}
