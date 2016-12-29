package io.github.rodyamirov.pascal.NodeVisitor;

import io.github.rodyamirov.pascal.Parser;
import io.github.rodyamirov.pascal.SymbolTable;
import io.github.rodyamirov.pascal.SymbolValue;
import io.github.rodyamirov.pascal.SymbolValueTable;
import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.TypeSpec;
import io.github.rodyamirov.pascal.tree.ExpressionNode;
import io.github.rodyamirov.pascal.tree.ProgramNode;
import io.github.rodyamirov.pascal.visitor.EvalVisitor;
import org.junit.Test;

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
    public void programTest1() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable desired;

        prog = "program test_1; "
                + "var a: integer;"
                + "begin "
                + " a := 1; "
                + " end .";
        symbolTable = SymbolTable.builder()
                .addSymbol(Token.ID("a"), TypeSpec.INTEGER).build();

        desired = new SymbolValueTable(symbolTable);
        desired.setValue(Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        doProgramTest(prog, desired);

        prog = "program _te;"
                + "var _ab, b: INTEGER;"
                + "begin "
                + "_ab:= 12+13;"
                + "b:= _ab-12 "
                + "end.";

        symbolTable = SymbolTable.builder()
                .addSymbol(Token.ID("_ab"), TypeSpec.INTEGER)
                .addSymbol(Token.ID("b"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(Token.ID("_ab"), SymbolValue.make(TypeSpec.INTEGER, 25));
        desired.setValue(Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 13));
        doProgramTest(prog, desired);

        prog = "program _3GBB;"
                + "var int: INTEGER;"
                + "begin\n"
                + "int := 12-6 div 2 ;"
                + "int := int-2 "
                + "end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(Token.ID("int"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(Token.ID("Int"), SymbolValue.make(TypeSpec.INTEGER, 7));
        doProgramTest(prog, desired);
    }

    @Test
    public void lotsOfNoops1() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable desired;

        prog = "progrAM NAME;"
                + "var a: integer;"
                + "begin ;;;"
                + "; a := 1 ;"
                + "; end .";
        symbolTable = SymbolTable.builder().addSymbol(Token.ID("a"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        doProgramTest(prog, desired);

        prog = "PROGRAM DEP ;"
                + "var _ab: Integer; b: Integer;"
                + "BEGIN ;;; {!!!}"
                + " ;; _ab:= 12+13; ;"
                + " ;; b:= _ab-12 ;"
                + "end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(Token.ID("_ab"), TypeSpec.INTEGER)
                .addSymbol(Token.ID("b"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(Token.ID("_ab"), SymbolValue.make(TypeSpec.INTEGER, 25));
        desired.setValue(Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 13));
        doProgramTest(prog, desired);

        prog = "PrograM S_ ;"
                + "var int: Integer; "
                + "BEGIN {what are we beginning!!!}"
                + ";;; int := 12-6 div 2 ; ;;"
                + " ;int := int-2 ;"
                + "  ;; ;;  end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(Token.ID("int"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(Token.ID("Int"), SymbolValue.make(TypeSpec.INTEGER, 7));
        doProgramTest(prog, desired);
    }

    @Test
    public void nested1() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable desired;

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

        prog = "program t2; "
                + "var a, b: integer; "
                + "Begin a := 2; begin a := 3; b := a-1; end; b := a+b; end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(Token.ID("a"), TypeSpec.INTEGER)
                .addSymbol(Token.ID("b"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 3));
        desired.setValue(Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 5));
        doProgramTest(prog, desired);

        prog = "program t3; "
                + "var a, b, c, d: Integer; "
                + "begin begin begin a:=1; b:=a-1; end; c := a-b; d := a*b-c*a+a*a*a end; end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(Token.ID("a"), TypeSpec.INTEGER)
                .addSymbol(Token.ID("b"), TypeSpec.INTEGER)
                .addSymbol(Token.ID("c"), TypeSpec.INTEGER)
                .addSymbol(Token.ID("d"), TypeSpec.INTEGER).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 0));
        desired.setValue(Token.ID("c"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(Token.ID("d"), SymbolValue.make(TypeSpec.INTEGER, 0));
        doProgramTest(prog, desired);
    }

    @Test
    public void mixedTypeTest1() {
        String prog;
        SymbolTable symbolTable;
        SymbolValueTable desired;

        prog = "program test1;"
                + "var a, b: Integer;"
                + "c, d: Real;"
                + "begin a := 1; b := 2; c := 3; d := 4 end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(Token.ID("a"), TypeSpec.INTEGER)
                .addSymbol(Token.ID("b"), TypeSpec.INTEGER)
                .addSymbol(Token.ID("c"), TypeSpec.REAL)
                .addSymbol(Token.ID("d"), TypeSpec.REAL).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(Token.ID("a"), SymbolValue.make(TypeSpec.INTEGER, 1));
        desired.setValue(Token.ID("b"), SymbolValue.make(TypeSpec.INTEGER, 2));
        desired.setValue(Token.ID("c"), SymbolValue.make(TypeSpec.REAL, 3.0f));
        desired.setValue(Token.ID("d"), SymbolValue.make(TypeSpec.REAL, 4.0f));

        doProgramTest(prog, desired);

        prog = "program test2;"
                + "var a, b: Real;"
                + "begin {test2}"
                + "a := 1.25; b := 4*a*a+4;"
                + "end.";
        symbolTable = SymbolTable.builder()
                .addSymbol(Token.ID("a"), TypeSpec.REAL)
                .addSymbol(Token.ID("b"), TypeSpec.REAL).build();
        desired = new SymbolValueTable(symbolTable);
        desired.setValue(Token.ID("a"), SymbolValue.make(TypeSpec.REAL, 1.25f));
        desired.setValue(Token.ID("b"), SymbolValue.make(TypeSpec.REAL, 4*1.25f*1.25f+4));

        doProgramTest(prog, desired);
    }
}
