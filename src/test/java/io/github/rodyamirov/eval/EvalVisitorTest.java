package io.github.rodyamirov.eval;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.parse.Parser;
import io.github.rodyamirov.symbols.Scope;
import io.github.rodyamirov.symbols.SymbolTable;
import io.github.rodyamirov.symbols.SymbolValue;
import io.github.rodyamirov.symbols.SymbolValueTable;
import io.github.rodyamirov.symbols.TypeSpec;
import io.github.rodyamirov.tree.ExpressionNode;
import io.github.rodyamirov.tree.ProgramNode;
import org.junit.Test;

import static io.github.rodyamirov.parse.Parser.ROOT_SCOPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/25/16.
 */
public class EvalVisitorTest {
    private void doExpressionTest(String toParse, SymbolTable symbolTable, SymbolValue desiredAnswer) {
        Parser parser = new Parser(toParse);
        ExpressionNode parseTree = parser.parseExpression();
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
        Parser parser = new Parser(toParse);
        ProgramNode parseTree = parser.parseProgram();
        SymbolValueTable actualEndState = EvalVisitor.evaluateProgram(parseTree);
        assertThat("State at the end was correct", actualEndState, is(desiredEndState));
    }

    @Test
    public void exprTest1() {
        SymbolTable symbolTable = SymbolTable.empty();
        doExpressionTest("12+-1", symbolTable, 11);
        doExpressionTest("-12", symbolTable, -12);
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
        SymbolTable symbolTable = SymbolTable.empty();

        doExpressionTest("1 mod 2", symbolTable, 1);
        doExpressionTest("5 mod 3", symbolTable, 2);
        doExpressionTest("(5 mod 3) mod 2", symbolTable, 0);
        doExpressionTest("5 mod (10 mod 4)", symbolTable, 1);
        doExpressionTest("10 - (7 mod 4)", symbolTable, 7);
    }

    @Test
    public void exprTest3() {
        SymbolTable symbolTable = SymbolTable.empty();

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
        SymbolTable symbolTable = SymbolTable.empty();

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
        SymbolTable symbolTable = SymbolTable.empty();

        doExpressionTest("3.1 < 12.0*4-2", symbolTable, true);
        doExpressionTest("12.0-7.9 >= 1*4+6", symbolTable, false);
    }

    @Test
    public void programTest1() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable desired;

        Scope progScope = ROOT_SCOPE.makeChildScope(Token.ID("test_1"));

        prog = "program test_1; "
                + "var a: integer;"
                + "begin "
                + " a := 1; "
                + " end .";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.INTEGER).build();

        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        doProgramTest(prog, desired);

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("_te"));
        prog = "program _te;"
                + "var _ab, b: INTEGER;"
                + "begin "
                + "_ab:= 12+13;"
                + "b:= _ab-12 "
                + "end.";

        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("_ab"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("_ab"), SymbolValue.make(TypeSpec.INTEGER, 25));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 13));
        doProgramTest(prog, desired);

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("_3GBB"));
        prog = "program _3GBB;"
                + "var int: INTEGER;"
                + "begin\n"
                + "int := 12-6 div 2 ;"
                + "int := int-2 "
                + "end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("int"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("Int"), SymbolValue.make(TypeSpec.INTEGER, 7));
        doProgramTest(prog, desired);
    }

    @Test
    public void lotsOfNoops1() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable desired;

        Scope progScope = ROOT_SCOPE.makeChildScope(Token.ID("NAME"));

        prog = "progrAM NAME;"
                + "var a: integer;"
                + "begin ;;;"
                + "; a := 1 ;"
                + "; end .";
        symbolTable = SymbolTable.builder().addSymbol(progScope, Token.ID("a"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        doProgramTest(prog, desired);

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("DEP"));
        prog = "PROGRAM DEP ;"
                + "var _ab: Integer; b: Integer;"
                + "BEGIN ;;; {!!!}"
                + " ;; _ab:= 12+13; ;"
                + " ;; b:= _ab-12 ;"
                + "end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("_ab"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("_ab"), SymbolValue.make(TypeSpec.INTEGER, 25));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 13));
        doProgramTest(prog, desired);

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("S_"));
        prog = "PrograM S_ ;"
                + "var int: Integer; "
                + "BEGIN {what are we beginning!!!}"
                + ";;; int := 12-6 div 2 ; ;;"
                + " ;int := int-2 ;"
                + "  ;; ;;  end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("int"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("Int"), SymbolValue.make(TypeSpec.INTEGER, 7));
        doProgramTest(prog, desired);
    }

    @Test
    public void nested1() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable desired;
        Scope progScope;

        prog = "program t1; "
                + "Begin end.";
        symbolTable = SymbolTable.empty();
        desired = new SymbolValueTable(symbolTable);
        doProgramTest(prog, desired);

        prog = "program t1; "
                + "Begin end.";
        symbolTable = SymbolTable.empty();
        desired = new SymbolValueTable(symbolTable);
        doProgramTest(prog, desired);

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("t2"));
        prog = "program t2; "
                + "var a, b: integer; "
                + "Begin a := 2; begin a := 3; b := a-1; end; b := a+b; end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 3));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 5));
        doProgramTest(prog, desired);

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("t3"));
        prog = "program t3; "
                + "var a, b, c, d: Integer; "
                + "begin begin begin a:=1; b:=a-1; end; c := a-b; d := a*b-c*a+a*a*a end; end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("c"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("d"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 0));
        desired.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.INTEGER, 0));
        doProgramTest(prog, desired);
    }

    @Test
    public void mixedTypeTest1() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable desired;
        Scope progScope;

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("test1"));
        prog = "program test1;"
                + "var a, b: Integer;"
                + "c, d: Real;"
                + "begin a := 1; b := 2; c := 3; d := 4 end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("c"), TypeSpec.REAL)
                .addSymbol(progScope, Token.ID("d"), TypeSpec.REAL).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 2));
        desired.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.REAL, 3.0f));
        desired.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.REAL, 4.0f));

        doProgramTest(prog, desired);
    }

    @Test
    public void mixedTypeTest2() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable desired;
        Scope progScope;

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("test2"));
        prog = "program test2;"
                + "var a, b: Real;"
                + "begin {test2}"
                + "a := 1.25; b := 4*a*a+4;"
                + "end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.REAL)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.REAL).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.REAL, 1.25f));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.REAL, 4*1.25f*1.25f+4));

        doProgramTest(prog, desired);
    }

    @Test
    public void boolTest1() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable desired;
        Scope progScope;

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("testBo"));
        prog = "program testBo;"
                + "var a, b, c, d, e, f: BooleaN;"
                + "begin { the program }"
                + "a := True; b := True; a := False;"
                + "c := a and b; d := a or b;"
                + "e := not a; f := not b "
                + "end .";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("c"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("d"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("e"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("f"), TypeSpec.BOOLEAN).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        desired.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        desired.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("e"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        desired.setValue(progScope, Token.ID("f"), SymbolValue.make(TypeSpec.BOOLEAN, false));

        doProgramTest(prog, desired);
    }

    @Test
    public void boolTest2() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable symbolValueTable;
        Scope progScope;

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("idk"));
        prog = "program idk {whatever};"
                + "var a, b: real; c, d: integer; e, f, g, h: boolean;"
                + "begin {my thing!}"
                + " a := 2; b:= 3.1; c := 12; d := c*c-4;"
                //  e := a < b or c < d;  <-- doesn't work; in pascal or's precedence is way too high and does "b or c"
                + " e := (a < b) or (c < d);"               // true
                + " f := a<>b;"                         // true
                + " g := (a > 5) or not (b < c);"          // false
                + " h := (a >= 12) or (b <= 2) or (c=12) and (d = c*c-4) " // true
                + "end {the whole thing{nested} syntax error! nope} .";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.REAL)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.REAL)
                .addSymbol(progScope, Token.ID("c"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("d"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("e"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("f"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("g"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("h"), TypeSpec.BOOLEAN).build();
        symbolValueTable = new SymbolValueTable(symbolTable);
        symbolValueTable.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.REAL, 2.0f));
        symbolValueTable.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.REAL, 3.1f));
        symbolValueTable.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.INTEGER, 12));
        symbolValueTable.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.INTEGER, 140));
        symbolValueTable.setValue(progScope, Token.ID("e"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        symbolValueTable.setValue(progScope, Token.ID("f"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        symbolValueTable.setValue(progScope, Token.ID("g"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        symbolValueTable.setValue(progScope, Token.ID("h"), SymbolValue.make(TypeSpec.BOOLEAN, true));

        doProgramTest(prog, symbolValueTable);
    }

    @Test
    public void boolTest3() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable symbolValueTable;
        Scope progScope;

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("idk3"));
        prog = "program idk3 {whatever};"
                + "var a, b: real; c, d: integer; e, f, g, h: boolean;"
                + "begin {my thing!}"
                + " a := 2; b:= 3.1; c := 12; d := c*c-4;"
                //  e := a < b or c < d;  <-- doesn't work; in pascal or's precedence is way too high and does "b or c"
                + " e := a<b or else c<d;"         // true
                + " f := a=b and then g;"          // true; note g not being defined isn't a problem because of short circuiting
                + " g := a > 5 or else not (b < c);"  // false
                + " h := a >= 12 or else b <= 2 or else (c=12 and then d = c*c-4) " // true
                + "end {the whole thing{nested} syntax error! nope} .";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.REAL)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.REAL)
                .addSymbol(progScope, Token.ID("c"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("d"), TypeSpec.INTEGER)
                .addSymbol(progScope, Token.ID("e"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("f"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("g"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("h"), TypeSpec.BOOLEAN).build();
        symbolValueTable = new SymbolValueTable(symbolTable);
        symbolValueTable.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.REAL, 2.0f));
        symbolValueTable.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.REAL, 3.1f));
        symbolValueTable.setValue(progScope, Token.ID("c"), SymbolValue.make(TypeSpec.INTEGER, 12));
        symbolValueTable.setValue(progScope, Token.ID("d"), SymbolValue.make(TypeSpec.INTEGER, 140));

        symbolValueTable.setValue(progScope, Token.ID("e"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        symbolValueTable.setValue(progScope, Token.ID("f"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        symbolValueTable.setValue(progScope, Token.ID("g"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        symbolValueTable.setValue(progScope, Token.ID("h"), SymbolValue.make(TypeSpec.BOOLEAN, true));

        doProgramTest(prog, symbolValueTable);
    }

    @Test
    public void ifTest1() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable symbolValueTable;
        Scope progScope;

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("thisTest"));
        prog = "program thisTest {it's so great};"
                + "var a: boolean; b: integer;"
                + "begin"
                + " a := true; "
                + " if a then"
                + "     b := 12"
                + " else"
                + "     b := a " // <-- note this would be a type error if it were executed
                + "end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.INTEGER).build();

        symbolValueTable = new SymbolValueTable(symbolTable);
        symbolValueTable.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        symbolValueTable.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 12));

        doProgramTest(prog, symbolValueTable);
    }

    @Test
    public void ifTest2() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable symbolValueTable;
        Scope progScope;

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("thisTest"));
        prog = "program thisTest {it's so great};"
                + "var a: boolean; b: integer;"
                + "begin"
                + " a := false; "
                + " if a then"
                + "     b := a " // <-- note this would be a type error if it were executed
                + " else"
                + "     b := 12"
                + "end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.INTEGER).build();

        symbolValueTable = new SymbolValueTable(symbolTable);
        symbolValueTable.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        symbolValueTable.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 12));

        doProgramTest(prog, symbolValueTable);
    }

    @Test
    public void ifTest3() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable symbolValueTable;
        Scope progScope;

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("thisTest"));
        prog = "program thisTest {it's so great};"
                + "var a, a2: boolean; b: integer;"
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
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("a2"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.INTEGER).build();

        symbolValueTable = new SymbolValueTable(symbolTable);
        symbolValueTable.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.BOOLEAN, false));
        symbolValueTable.setValue(progScope, Token.ID("a2"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        symbolValueTable.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 12));

        doProgramTest(prog, symbolValueTable);
    }

    @Test
    public void ifTest4() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable symbolValueTable;
        Scope progScope;

        progScope = ROOT_SCOPE.makeChildScope(Token.ID("thisTest"));
        prog = "program thisTest {it's so great};"
                + "var a, a2: boolean; b: integer;"
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
        symbolTable = SymbolTable.builder()
                .addSymbol(progScope, Token.ID("a"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("a2"), TypeSpec.BOOLEAN)
                .addSymbol(progScope, Token.ID("b"), TypeSpec.INTEGER).build();

        symbolValueTable = new SymbolValueTable(symbolTable);
        symbolValueTable.setValue(progScope, Token.ID("a"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        symbolValueTable.setValue(progScope, Token.ID("a2"), SymbolValue.make(TypeSpec.BOOLEAN, true));
        symbolValueTable.setValue(progScope, Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 12));

        doProgramTest(prog, symbolValueTable);
    }
}
