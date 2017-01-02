package io.github.rodyamirov.eval;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.parse.Parser;
import io.github.rodyamirov.symbols.Scope;
import io.github.rodyamirov.symbols.ScopeAssigner;
import io.github.rodyamirov.symbols.SymbolTable;
import io.github.rodyamirov.symbols.SymbolTableBuilder;
import io.github.rodyamirov.symbols.SymbolValue;
import io.github.rodyamirov.symbols.SymbolValueTable;
import io.github.rodyamirov.symbols.TypeSpec;
import io.github.rodyamirov.tree.ExpressionNode;
import io.github.rodyamirov.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.tree.ProgramNode;
import org.junit.Test;

import static io.github.rodyamirov.symbols.ScopeAssigner.ROOT_SCOPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/25/16.
 */
public class EvalVisitorTest {
    private void doExpressionTest(String toParse, SymbolTable symbolTable, SymbolValue desiredAnswer) {
        ExpressionNode parseTree = Parser.parseExpression(toParse);
        ScopeAssigner.assignScopes(ROOT_SCOPE, parseTree);
        SymbolValue actualAnswer = EvalVisitor.evaluateExpression(parseTree, symbolTable);
        assertThat("Expression was correctly evaluated", actualAnswer, is(desiredAnswer));
    }

    private void doExpressionTest(String toParse, SymbolTable symbolTable, int desiredAnswer) {
        doExpressionTest(toParse, symbolTable, SymbolValue.make(TypeSpec.INTEGER, desiredAnswer));
    }

    private void doExpressionTest(String toParse, SymbolTable symbolTable, float desiredAnswer) {
        doExpressionTest(toParse, symbolTable, SymbolValue.make(TypeSpec.REAL, desiredAnswer));
    }

    private void doExpressionTest(String toParse, SymbolTable symbolTable, boolean desiredAnswer) {
        doExpressionTest(toParse, symbolTable, SymbolValue.make(TypeSpec.BOOLEAN, desiredAnswer));
    }

    private void doProgramTest(String toParse, SymbolValueTable desiredEndState) {
        ProgramNode parseTree = Parser.parseProgram(toParse);
        ScopeAssigner.assignScopes(ROOT_SCOPE, parseTree);
        SymbolTable symbolTable = SymbolTableBuilder.buildFrom(parseTree);
        SymbolValueTable actualEndState = EvalVisitor.evaluateProgram(parseTree, symbolTable);
        assertThat("State at the end was correct", actualEndState, is(desiredEndState));
    }

    private SymbolValue<ProgramNode> makeProgram(Scope rootScope, String progText) {
        ProgramNode programNode = Parser.parseProgram(progText);
        ScopeAssigner.assignScopes(rootScope, programNode);
        return SymbolValue.make(TypeSpec.PROGRAM, programNode);
    }

    private SymbolValue<ProcedureDeclarationNode> makeProcedure(Scope rootScope, String procedureText) {
        ProcedureDeclarationNode procedureDeclarationNode = Parser.parseProcedure(procedureText);
        ScopeAssigner.assignScopes(rootScope, procedureDeclarationNode);
        return SymbolValue.make(TypeSpec.PROCEDURE, procedureDeclarationNode);
    }

    private SymbolTable makeSymbolTable(String programText) {
        ProgramNode programNode = Parser.parseProgram(programText);
        ScopeAssigner.assignScopes(ROOT_SCOPE, programNode);
        return SymbolTableBuilder.buildFrom(programNode);
    }

    @Test
    public void exprTest1() {
        SymbolTable symbolTable = SymbolTable.builder().build();
        doExpressionTest("-12", symbolTable, -12);
        doExpressionTest("12+-1", symbolTable, 11);
        doExpressionTest("1*-13", symbolTable, -13);
        doExpressionTest("13*-1", symbolTable, -13);
        doExpressionTest("-1--3", symbolTable, 2);
        doExpressionTest("(-13)div 2", symbolTable, -6);
        doExpressionTest("((13*-1)div (-1--3))", symbolTable, -6);
        doExpressionTest("1+((13*-1)div(-1--3))", symbolTable, -5);
        doExpressionTest("+-(1-13)", symbolTable, 12);

        doExpressionTest("1/2", symbolTable, 0.5f);
        doExpressionTest("1.12", symbolTable, 1.12f);
        doExpressionTest("1+1.12", symbolTable, 2.12f);
        doExpressionTest("-12.2 * 15", symbolTable, -12.2f * 15);
        doExpressionTest("-(5/2) + 12.1", symbolTable, -2.5f + 12.1f);
        doExpressionTest("+-+--+-12787.1", symbolTable, 12787.1f);
        doExpressionTest("2.3 - 17.1", symbolTable, 2.3f - 17.1f);
        doExpressionTest("2.3/1.12", symbolTable, 2.3f / 1.12f);

        doExpressionTest("1+(2.3*4) / 0.7", symbolTable, 1 + (9.2f / 0.7f));
        doExpressionTest("(1 div 2) + (1/2) - 1.0", symbolTable, -0.5f);
    }

    @Test
    public void exprTest2() {
        SymbolTable symbolTable = SymbolTable.builder().build();

        doExpressionTest("1 mod 2", symbolTable, 1);
        doExpressionTest("5 mod 3", symbolTable, 2);
        doExpressionTest("(5 mod 3) mod 2", symbolTable, 0);
        doExpressionTest("5 mod (10 mod 4)", symbolTable, 1);
        doExpressionTest("10 - (7 mod 4)", symbolTable, 7);
    }

    @Test
    public void exprTest3() {
        SymbolTable symbolTable = SymbolTable.builder().build();

        doExpressionTest("true", symbolTable, true);
        doExpressionTest("TrUe", symbolTable, true);
        doExpressionTest("FALSE", symbolTable, false);

        doExpressionTest("1 < 1", symbolTable, false);
        doExpressionTest("1 < 2", symbolTable, true);
        doExpressionTest("2 < 2", symbolTable, false);
        doExpressionTest("2 < 1", symbolTable, false);

        doExpressionTest("1 <= 1", symbolTable, true);
        doExpressionTest("1 <= 2", symbolTable, true);
        doExpressionTest("2 <= 2", symbolTable, true);
        doExpressionTest("2 <= 1", symbolTable, false);

        doExpressionTest("1 <> 1", symbolTable, false);
        doExpressionTest("1 <> 2", symbolTable, true);
        doExpressionTest("2 <> 2", symbolTable, false);
        doExpressionTest("2 <> 1", symbolTable, true);

        doExpressionTest("1 > 1", symbolTable, false);
        doExpressionTest("1 > 2", symbolTable, false);
        doExpressionTest("2 > 2", symbolTable, false);
        doExpressionTest("2 > 1", symbolTable, true);

        doExpressionTest("1 >= 1", symbolTable, true);
        doExpressionTest("1 >= 2", symbolTable, false);
        doExpressionTest("2 >= 2", symbolTable, true);
        doExpressionTest("2 >= 1", symbolTable, true);

        doExpressionTest("1 = 1", symbolTable, true);
        doExpressionTest("1 = 2", symbolTable, false);
        doExpressionTest("2 = 2", symbolTable, true);
        doExpressionTest("2 = 1", symbolTable, false);
    }

    @Test
    public void exprTest4() {
        SymbolTable symbolTable = SymbolTable.builder().build();

        doExpressionTest("3.1 < 3", symbolTable, false);
        doExpressionTest("3.1 <= 3", symbolTable, false);
        doExpressionTest("3.1 <> 3", symbolTable, true);
        doExpressionTest("3.1 > 3", symbolTable, true);
        doExpressionTest("3.1 >= 3", symbolTable, true);
        doExpressionTest("3.1 = 3", symbolTable, false);

        doExpressionTest("3.0 < 3", symbolTable, false);
        doExpressionTest("3.0 <= 3", symbolTable, true);
        doExpressionTest("3.0 <> 3", symbolTable, false);
        doExpressionTest("3.0 = 3", symbolTable, true);
        doExpressionTest("3.0 > 3", symbolTable, false);
        doExpressionTest("3.0 >= 3", symbolTable, true);
    }

    @Test
    public void exprTest5() {
        SymbolTable symbolTable = SymbolTable.builder().build();

        doExpressionTest("3.1 < 12.0*4-2", symbolTable, true);
        doExpressionTest("12.0-7.9 >= 1*4+6", symbolTable, false);
    }

    @Test
    public void programTest1() {
        String prog;
        SymbolValueTable desired;
        Token<String> progName;
        Scope progScope;

        progName = Token.ID("test_1");
        progScope = ROOT_SCOPE.makeChildScope(progName);

        prog = "program test_1; "
                + "var a: integer;"
                + "begin "
                + " a := 1; "
                + " end .";
        desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        doProgramTest(prog, desired);

        progName = Token.ID("_te");
        progScope = ROOT_SCOPE.makeChildScope(progName);
        prog = "program _te;"
                + "var _ab, b: INTEGER;"
                + "begin "
                + "_ab:= 12+13;"
                + "b:= _ab-12 "
                + "end.";
        desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("_ab"), SymbolValue.make(TypeSpec.INTEGER, 25));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 13));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        doProgramTest(prog, desired);

        progName = Token.ID("_3GBB");
        progScope = ROOT_SCOPE.makeChildScope(progName);
        prog = "program _3GBB;"
                + "var int: INTEGER;"
                + "begin\n"
                + "int := 12-6 div 2 ;"
                + "int := int-2 "
                + "end.";
        desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("Int"), SymbolValue.make(TypeSpec.INTEGER, 7));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        doProgramTest(prog, desired);
    }

    @Test
    public void lotsOfNoops1() {
        String prog;
        SymbolValueTable desired;
        Scope progScope;

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("NAME"));
        prog = "progrAM NAME;"
                + "var a: integer;"
                + "begin ;;;"
                + "; a := 1 ;"
                + "; end .";
        desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(ROOT_SCOPE, Token.ID("NAME"), makeProgram(ROOT_SCOPE, prog));
        doProgramTest(prog, desired);

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("DEP"));
        prog = "PROGRAM DEP ;"
                + "var _ab: Integer; b: Integer;"
                + "BEGIN ;;; {!!!}"
                + " ;; _ab:= 12+13; ;"
                + " ;; b:= _ab-12 ;"
                + "end.";
        desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("_ab"), SymbolValue.make(TypeSpec.INTEGER, 25));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 13));
        desired.setValue(ROOT_SCOPE, Token.ID("DEP"), makeProgram(ROOT_SCOPE, prog));
        doProgramTest(prog, desired);

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("S_"));
        prog = "PrograM S_ ;"
                + "var int: Integer; "
                + "BEGIN {what are we beginning!!!}"
                + ";;; int := 12-6 div 2 ; ;;"
                + " ;int := int-2 ;"
                + "  ;; ;;  end.";
        desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("Int"), SymbolValue.make(TypeSpec.INTEGER, 7));
        desired.setValue(ROOT_SCOPE, Token.ID("S_"), makeProgram(ROOT_SCOPE, prog));
        doProgramTest(prog, desired);
    }

    @Test
    public void nested1() {
        String prog;
        SymbolValueTable desired;
        Scope progScope;
        Token<String> progName;

        progName = Token.ID("t1");
        progScope = ROOT_SCOPE.makeChildScope(progName);
        prog = "program t1; "
                + "Begin end.";
        desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        doProgramTest(prog, desired);

        progName = Token.ID("t1");
        progScope = ROOT_SCOPE.makeChildScope(progName);
        prog = "program t1; "
                + "Begin end.";
        desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        doProgramTest(prog, desired);

        progName = Token.ID("t2");
        progScope = ROOT_SCOPE.makeChildScope(progName);
        prog = "program t2; "
                + "var a, b: integer; "
                + "Begin a := 2; begin a := 3; b := a-1; end; b := a+b; end.";
        desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 3));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 5));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        doProgramTest(prog, desired);

        progName = Token.ID("t3");
        progScope = ROOT_SCOPE.makeChildScope(progName);
        prog = "program t3; "
                + "var a, b, c, d: Integer; "
                + "begin begin begin a:=1; b:=a-1; end; c := a-b; d := a*b-c*a+a*a*a end; end.";
        desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 0));
        desired.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.INTEGER, 0));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        doProgramTest(prog, desired);
    }

    @Test
    public void mixedTypeTest1() {
        Token<String> progName = Token.ID("test1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program test1;"
                + "var a, b: Integer; c, d: Real;"
                + "begin a := 1; b := 2; c := 3; d := 4 end.";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 2));
        desired.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.REAL, 3.0f));
        desired.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.REAL, 4.0f));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));

        doProgramTest(prog, desired);
    }

    @Test
    public void mixedTypeTest2() {
        Token<String> progName = Token.ID("test2");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program test2;"
                + "var a, b: Real;"
                + "begin {test2}"
                + " a := 1.25; b := 4*a*a+4;"
                + "end.";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.REAL, 1.25f));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.REAL, 4 * 1.25f * 1.25f + 4));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));

        doProgramTest(prog, desired);
    }

    @Test
    public void boolTest1() {
        Token<String> progName = Token.ID("testBo");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program testBo;"
                + " var a, b, c, d, e, f: BooleaN;"
                + "begin { the program }"
                + " a := True; b := True; a := False;"
                + " c := a and b; d := a or b;"
                + " e := not a; f := not b "
                + "end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        desired.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("e"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("f"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));

        doProgramTest(prog, desired);
    }

    @Test
    public void boolTest2() {
        Token<String> progName = Token.ID("idk");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program idk {whatever};"
                + " var a, b: real; c, d: integer; e, f, g, h: boolean;"
                + "begin {my thing!}"
                + " a := 2; b:= 3.1; c := 12; d := c*c-4;"
                //  e := a < b or c < d;  <-- doesn't work; in pascal or's precedence is way too high and does "b or c"
                + " e := (a < b) or (c < d);"               // true
                + " f := a<>b;"                         // true
                + " g := (a > 5) or not (b < c);"          // false
                + " h := (a >= 12) or (b <= 2) or (c=12) and (d = c*c-4) " // true
                + "end {the whole thing{nested} syntax error! nope} .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.REAL, 2.0f));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.REAL, 3.1f));
        desired.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.INTEGER, 12));
        desired.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.INTEGER, 140));
        desired.setValue(progScope, Token.ID("e"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("f"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("g"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        desired.setValue(progScope, Token.ID("h"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));

        doProgramTest(prog, desired);
    }

    @Test
    public void boolTest3() {
        Token<String> progName = Token.ID("idk3");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program idk3 {whatever};"
                + " var a, b: real; c, d: integer; e, f, g, h: boolean;"
                + "begin {my thing!}"
                + " a := 2; b:= 3.1; c := 12; d := c*c-4;"
                //  e := a < b or c < d;  <-- doesn't work; in pascal or's precedence is way too high and does "b or c"
                + " e := a<b or else c<d;"         // true
                + " f := a=b and then g;"
                // true; note g not being defined isn't a problem because of short circuiting
                + " g := a > 5 or else not (b < c);"  // false
                + " h := a >= 12 or else b <= 2 or else (c=12 and then d = c*c-4) " // true
                + "end {the whole thing{nested} syntax error! nope} .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.REAL, 2.0f));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.REAL, 3.1f));
        desired.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.INTEGER, 12));
        desired.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.INTEGER, 140));

        desired.setValue(progScope, Token.ID("e"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("f"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        desired.setValue(progScope, Token.ID("g"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        desired.setValue(progScope, Token.ID("h"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));

        doProgramTest(prog, desired);
    }

    @Test
    public void ifTest1() {
        Token<String> progName = Token.ID("thisTest");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program thisTest {it's so great};"
                + " var a: boolean; b: integer;"
                + "begin"
                + " a := true; "
                + " if a then"
                + "     b := 12"
                + " else"
                + "     b := a " // <-- note this would be a type error if it were executed
                + "end.";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 12));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));

        doProgramTest(prog, desired);
    }

    @Test
    public void ifTest2() {
        Token<String> progName = Token.ID("thisTest");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program thisTest {it's so great};"
                + " var a: boolean; b: integer;"
                + "begin"
                + " a := false; "
                + " if a then"
                + "     b := a " // <-- note this would be a type error if it were executed
                + " else"
                + "     b := 12"
                + "end.";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 12));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));

        doProgramTest(prog, desired);
    }

    @Test
    public void ifTest3() {
        Token<String> progName = Token.ID("thisTest");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program thisTest {it's so great};"
                + " var a, a2: boolean; b: integer;"
                + "begin"
                + " a := -7 > 15; "
                + " if a then"
                + "     if a2 then" // <-- error, never executed
                + "         b := 13"
                + "     else"
                + "         b := 14"
                + " else"
                + "     begin"
                + "         a2 := 1 < 10;"
                + "         if a2 then"
                + "             b := 12"
                + "         else"
                + "             c := 1" // <-- also an error, never executed
                + "     end {of the else block}"
                + "end.";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        desired.setValue(progScope, Token.ID("a2"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 12));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));

        doProgramTest(prog, desired);
    }

    @Test
    public void ifTest4() {
        Token<String> progName = Token.ID("thisTest");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program thisTest {it's so great};"
                + " var a, a2: boolean; b: integer;"
                + "begin"
                + " a := -7 < 15; "
                + " if a then"
                + "     begin"
                + "         a2 := 1 < 10;"
                + "         if a2 then"
                + "             b := 12"
                + "         else"
                + "             c := 1"
                + "     end {of the if block}" // <-- also an error, never executed
                + " else"
                + "     if a2 then" // <-- error, never executed
                + "         b := 13"
                + "     else"
                + "         b := 14"
                + "end.";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("a2"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 12));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));

        doProgramTest(prog, desired);
    }

    @Test
    public void whileTest1() {
        Token<String> procName = Token.ID("proc1");
        String procText = ""
                + "procedure proc1;"
                + " begin {proc1}"
                + "     while a > 3 do a := a-2"
                + " end {proc1};";

        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program whileTest1;"
                + " var a: integer;"
                + procText
                + "begin {whileTest1}"
                + " a := 12;"
                + " proc1();"
                + " a := a+3;"
                + " proc1();"
                + "end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, procName, makeProcedure(progScope, procText));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 3));
        doProgramTest(prog, desired);
    }

    @Test
    public void doUntilTest1() {
        Token<String> procName = Token.ID("proc1");
        String procText = ""
                + "procedure proc1;"
                + " begin {proc1}"
                + "     do a := a-2 until a <= 3"
                + " end {proc1};";

        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + "program whileTest1;"
                + " var a: integer;"
                + procText
                + "begin {whileTest1}"
                + " a := 12;"
                + " proc1();"
                + " a := a+3;"
                + " proc1();"
                + "end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, procName, makeProcedure(progScope, procText));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 3));
        doProgramTest(prog, desired);
    }

    @Test
    public void whileContinueTest1() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     while (a >= 0) and (a<= 30) do"
                + "         begin"
                + "             a := 40;"
                + "             continue;"
                + "             a := -15"
                + "         end"
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 40));
        doProgramTest(prog, desired);
    }

    @Test
    public void whileContinueTest2() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     while (a >= 0) and (a <= 30) do"
                + "         begin"
                + "             a := a+3;"
                + "             continue;"
                + "             a := -15"
                + "         end"
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 33));
        doProgramTest(prog, desired);
    }

    @Test
    public void whileBreakTest1() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     while (a >= 0) and (a <= 30) do"
                + "         begin"
                + "             a := 40;"
                + "             break;"
                + "             a := -15"
                + "         end"
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 40));
        doProgramTest(prog, desired);
    }

    @Test
    public void whileBreakTest2() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     while (a >= 0) and (a <= 30) do"
                + "         begin"
                + "             a := a+3;"
                + "             break;"
                + "             a := -15"
                + "         end"
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 15));
        doProgramTest(prog, desired);
    }

    @Test
    public void doUntilContinueTest1() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     do"
                + "         begin"
                + "             a := 40;"
                + "             continue;"
                + "             a := -15"
                + "         end"
                + "     until (a<0) or (a>30)"
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 40));
        doProgramTest(prog, desired);
    }

    @Test
    public void doUntilContinueTest2() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     do"
                + "         begin"
                + "             a := a+3;"
                + "             continue;"
                + "             a := -15"
                + "         end"
                + "     until (a<0) or (a>30)"
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 33));
        doProgramTest(prog, desired);
    }

    @Test
    public void doUntilContinueTest3() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     do"
                + "         begin"
                + "             begin" // just to check it can bust through multiple compounds
                + "                 a := a+3;"
                + "                 continue;"
                + "                 a := -15"
                + "             end;"
                + "         end"
                + "     until (a<0) or (a>30)"
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 33));
        doProgramTest(prog, desired);
    }

    @Test
    public void doUntilContinueTest4() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer; bad, good: boolean;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     bad := false;"
                + "     good := false;"
                + "     do"
                + "         begin"
                + "             do"
                + "                 begin"
                + "                     a := a+3;"
                + "                     continue;"
                + "                     bad := true;"
                + "                 end"
                + "             until a > 30;"
                + "             good := true"  // make sure continue doesn't skip this line
                + "         end"
                + "     until true" // not much of a loop
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 33));
        desired.setValue(progScope, Token.ID("good"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("bad"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        doProgramTest(prog, desired);
    }

    @Test
    public void doUntilBreakTest1() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     do"
                + "         begin"
                + "             a := 40;"
                + "             break;"
                + "             a := -15"
                + "         end"
                + "     until (a<0) or (a>30)"
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 40));
        doProgramTest(prog, desired);
    }

    @Test
    public void doUntilBreakTest2() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     do"
                + "         begin"
                + "             a := a+3;"
                + "             break;"
                + "             a := -15"
                + "         end"
                + "     until (a<0) or (a>30)"
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 15));
        doProgramTest(prog, desired);
    }

    @Test
    public void doUntilBreakTest3() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     do"
                + "         begin"
                + "             begin" // just to check it can bust through multiple compounds
                + "                 a := a+3;"
                + "                 break;"
                + "                 a := -15"
                + "             end;"
                + "         end"
                + "     until (a<0) or (a>30)"
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 15));
        doProgramTest(prog, desired);
    }

    @Test
    public void doUntilBreakTest4() {
        Token<String> progName = Token.ID("whileTest1");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        String prog = ""
                + " program whileTest1;"
                + "     var a: integer; bad, good: boolean;"
                + " begin {whileTest1}"
                + "     a := 12;"
                + "     bad := false;"
                + "     good := false;"
                + "     do"
                + "         begin"
                + "             do"
                + "                 begin"
                + "                     a := a+3;"
                + "                     break;"
                + "                     bad := true;"
                + "                 end"
                + "             until a > 30;"
                + "             good := true"  // make sure break doesn't break this line
                + "         end"
                + "     until true" // not much of a loop
                + " end .";
        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(prog));
        desired.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, prog));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 15));
        desired.setValue(progScope, Token.ID("good"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("bad"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        doProgramTest(prog, desired);
    }

    @Test
    public void forLoopTest1() {
        String progText = ""
                + "program a;"
                + "     var a, b: integer; c: integer; d: real;"
                + "begin"
                + "     a := 1;"
                + "     b := 30;"
                + "     c := 0;"
                + "     d := b+1;"
                + "     for a:=b*b to 913 do"
                + "         c := c+1;"
                + "     for a := b downto 10 do"
                + "         d := d-1"
                + "end .";

        Token<String> progName = Token.ID("a");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);
        SymbolValueTable symbolValueTable = new SymbolValueTable(makeSymbolTable(progText));
        symbolValueTable.setValue(ROOT_SCOPE, progName, makeProgram(ROOT_SCOPE, progText));
        symbolValueTable.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 10));
        symbolValueTable.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 30));
        symbolValueTable.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.INTEGER, 14));
        symbolValueTable.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.REAL, 10.0f));

        doProgramTest(progText, symbolValueTable);
    }

    @Test
    public void procCallBreakTest() {
        String prog;

        prog = ""
                + "program badBreakTest;"
                + "procedure um;"
                + " begin break end;"
                + "begin {program now}"
                + " while true do"
                + "     um()" // one statement loop
                + "end .";

        ProgramNode programNode = makeProgram(ROOT_SCOPE, prog).value;
        SymbolTable symbolTable = SymbolTableBuilder.buildFrom(programNode);

        try {
            EvalVisitor.evaluateProgram(programNode, symbolTable);
            assertThat("Illegal break; should have thrown an error", false, is(true));
        } catch (IllegalStateException e) {
            // this is all good
        }

        // but also check that breaks inside procedure calls are still OK
        prog = ""
                + "program goodBreakTest;"
                + "procedure um;"
                + " begin while true do break end;"
                + "begin um() end .";

        programNode = makeProgram(ROOT_SCOPE, prog).value;
        symbolTable = SymbolTableBuilder.buildFrom(programNode);

        // no error needed
        EvalVisitor.evaluateProgram(programNode, symbolTable);
    }

    @Test
    public void procCallContinueTest() {
        String prog;

        prog = ""
                + "program badBreakTest;"
                + "procedure um;"
                + " begin continue end;"
                + "begin {program now}"
                + " while true do"
                + "     um()" // one statement loop
                + "end .";

        ProgramNode programNode = makeProgram(ROOT_SCOPE, prog).value;
        SymbolTable symbolTable = SymbolTableBuilder.buildFrom(programNode);

        try {
            EvalVisitor.evaluateProgram(programNode, symbolTable);
            assertThat("Illegal continue; should have thrown an error", false, is(true));
        } catch (IllegalStateException e) {
            // this is all good
        }

        // but some continues are ok
        prog = ""
                + "program badBreakTest;"
                + "procedure um;"
                + " begin do continue until true end;"
                + "begin {program now}"
                + " um()"
                + "end .";

        programNode = makeProgram(ROOT_SCOPE, prog).value;
        symbolTable = SymbolTableBuilder.buildFrom(programNode);
        EvalVisitor.evaluateProgram(programNode, symbolTable);
    }

    @Test
    public void procCallTest1() {
        Token<String> progName = Token.ID("progIt");
        Scope progScope = ROOT_SCOPE.makeChildScope(progName);

        Token<String> procName1 = Token.ID("proc_1");
        Scope procScope1 = progScope.makeChildScope(procName1);

        Token<String> procName2 = Token.ID("proc_2");
        Scope procScope2 = procScope1.makeChildScope(procName2);

        Token<String> procName3 = Token.ID("proc_3");
        Scope procScope3 = progScope.makeChildScope(procName3);

        Token<String> procName4 = Token.ID("proc_4");
        Scope procScope4 = procScope3.makeChildScope(procName4);

        String procText2 = "procedure proc_2; var a, b: Integer; begin a := 1; c := c+a+1; end;";
        ProcedureDeclarationNode procNode2 = makeProcedure(procScope1, procText2).value;

        String procText1 = String.format( // in the end, the effect is: a += 17
                "procedure proc_1; var c: Real; %s begin c:= 12; proc_2(); proc_2(); a := a+c+1; end;",
                procText2);
        ProcedureDeclarationNode procNode1 = makeProcedure(progScope, procText1).value;

        String procText4 = "procedure proc_4; var c: Real; begin c := 12; end;";
        ProcedureDeclarationNode procNode4 = makeProcedure(procScope3, procText4).value;

        String procText3 = String.format( // in the end, the effect is: a -= 1
                "procedure proc_3; var c: Boolean; %s begin c := true; if c then a := a-1 end;",
                procText4);
        ProcedureDeclarationNode procNode3 = makeProcedure(progScope, procText3).value;

        String progText = String.format(
                "program progIt; var a: REAL; %s %s begin a:= 0; proc_1(); proc_1(); proc_3(); proc_1(); end.",
                procText1, procText3);
        ProgramNode programNode = makeProgram(ROOT_SCOPE, progText).value;


        SymbolValueTable desired = new SymbolValueTable(makeSymbolTable(progText));
        desired.setValue(ROOT_SCOPE, progName, SymbolValue.make(TypeSpec.PROGRAM, programNode));
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.REAL, 50.0f));

        desired.setValue(progScope, procName1, SymbolValue.make(TypeSpec.PROCEDURE, procNode1));
        desired.setValue(procScope1, procName2, SymbolValue.make(TypeSpec.PROCEDURE, procNode2));

        desired.setValue(progScope, procName3, SymbolValue.make(TypeSpec.PROCEDURE, procNode3));
        desired.setValue(procScope3, procName4, SymbolValue.make(TypeSpec.PROCEDURE, procNode4));

        doProgramTest(progText, desired);
    }
}
