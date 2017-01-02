package io.github.rodyamirov.analysis;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.parse.Parser;
import io.github.rodyamirov.symbols.Scope;
import io.github.rodyamirov.symbols.ScopeAssigner;
import io.github.rodyamirov.symbols.SymbolTable;
import io.github.rodyamirov.symbols.SymbolTableBuilder;
import io.github.rodyamirov.symbols.TypeSpec;
import io.github.rodyamirov.tree.ExpressionNode;
import io.github.rodyamirov.tree.ProgramNode;
import org.junit.Test;

import java.util.List;

import static io.github.rodyamirov.symbols.ScopeAssigner.ROOT_SCOPE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by richard.rast on 1/2/17.
 */
public class TypeCheckerTest {
    private void checkProgram(String programText, boolean expectErrors) {
        ProgramNode programNode = Parser.parseProgram(programText);
        ScopeAssigner.assignScopes(ROOT_SCOPE, programNode);
        SymbolTable symbolTable = SymbolTableBuilder.buildFrom(programNode);

        List<ErrorMessage> errorMessages = TypeChecker.assignTypes(programNode, symbolTable);

        assertThat(errorMessages.size() > 0, is(expectErrors));
    }

    private ExpressionNode makeExpression(String expressionText) {
        ExpressionNode expressionNode = Parser.parseExpression(expressionText);
        ScopeAssigner.assignScopes(ROOT_SCOPE, expressionNode);
        SymbolTable symbolTable = SymbolTable.builder().build();

        List<ErrorMessage> errors = TypeChecker.assignTypes(expressionNode, symbolTable);

        if (errors.isEmpty()) {
            return expressionNode;
        } else {
            throw new RuntimeException("Unexpected exception");
        }
    }

    private ExpressionNode makeExpression(String expressionText, String variableDeclarations, Scope programScope) {
        ProgramNode programNode = Parser.parseProgram(variableDeclarations);
        ScopeAssigner.assignScopes(ROOT_SCOPE, programNode);
        SymbolTable symbolTable = SymbolTableBuilder.buildFrom(programNode);

        ExpressionNode expressionNode = Parser.parseExpression(expressionText);
        ScopeAssigner.assignScopes(programScope, expressionNode);

        List<ErrorMessage> errors = TypeChecker.assignTypes(expressionNode, symbolTable);

        if (errors.isEmpty()) {
            return expressionNode;
        } else {
            throw new RuntimeException("Unexpected exception");
        }
    }

    private void doExpressionTypeFail(String expressionText) {
        ExpressionNode expressionNode = Parser.parseExpression(expressionText);
        ScopeAssigner.assignScopes(ROOT_SCOPE, expressionNode);
        SymbolTable symbolTable = SymbolTable.builder().build();

        List<ErrorMessage> errors = TypeChecker.assignTypes(expressionNode, symbolTable);

        assertThat("Got an error", errors.size() > 0, is(true));
    }

    private void doExpressionTypeFail(String expressionText, String variableDeclarations, Scope programScope) {
        ProgramNode programNode = Parser.parseProgram(variableDeclarations);
        ScopeAssigner.assignScopes(ROOT_SCOPE, programNode);
        SymbolTable symbolTable = SymbolTableBuilder.buildFrom(programNode);

        ExpressionNode expressionNode = Parser.parseExpression(expressionText);
        ScopeAssigner.assignScopes(programScope, expressionNode);

        List<ErrorMessage> errors = TypeChecker.assignTypes(expressionNode, symbolTable);

        assertThat("Got an error", errors.size() > 0, is(true));
    }

    @Test
    public void terminalTest() {
        assertThat(makeExpression("1").outputType, is(TypeSpec.INTEGER));
        assertThat(makeExpression("131").outputType, is(TypeSpec.INTEGER));
        assertThat(makeExpression("121").outputType, is(TypeSpec.INTEGER));
        assertThat(makeExpression("41839").outputType, is(TypeSpec.INTEGER));
        assertThat(makeExpression("  389478913").outputType, is(TypeSpec.INTEGER));

        assertThat(makeExpression("1.0").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("321781.3212").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("412897.134").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("413.1341435").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("153.1531").outputType, is(TypeSpec.REAL));

        assertThat(makeExpression("true").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("false").outputType, is(TypeSpec.BOOLEAN));
    }

    @Test
    public void binopTest() {
        assertThat(makeExpression("1+12").outputType, is(TypeSpec.INTEGER));
        assertThat(makeExpression("1-12").outputType, is(TypeSpec.INTEGER));
        assertThat(makeExpression("1*12").outputType, is(TypeSpec.INTEGER));

        assertThat(makeExpression("1.0+12.0").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("1.12-12.1553").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("1.735*12.37557").outputType, is(TypeSpec.REAL));

        assertThat(makeExpression("1.0+12").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("1.12-12").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("1.735*12").outputType, is(TypeSpec.REAL));

        assertThat(makeExpression("1 + 12.0").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("1 - 12.1553").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("1 * 12.37557").outputType, is(TypeSpec.REAL));

        assertThat(makeExpression("1 / 12.513").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("1.35513 / 12").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("1.35513 / 12.513").outputType, is(TypeSpec.REAL));

        assertThat(makeExpression("1 div 12").outputType, is(TypeSpec.INTEGER));
        assertThat(makeExpression("1 mod 12").outputType, is(TypeSpec.INTEGER));

        assertThat(makeExpression("true and false").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("true or false").outputType, is(TypeSpec.BOOLEAN));

        assertThat(makeExpression("1 < 2").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1.0 < 2").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1 < 2.0").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1.0 < 2.0").outputType, is(TypeSpec.BOOLEAN));

        assertThat(makeExpression("1 <= 2").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1.0 <= 2").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1 <= 2.0").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1.0 <= 2.0").outputType, is(TypeSpec.BOOLEAN));

        assertThat(makeExpression("1 > 2").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1.0 > 2").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1 > 2.0").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1.0 > 2.0").outputType, is(TypeSpec.BOOLEAN));

        assertThat(makeExpression("1 >= 2").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1.0 >= 2").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1 >= 2.0").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1.0 >= 2.0").outputType, is(TypeSpec.BOOLEAN));

        assertThat(makeExpression("true and then false").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("true or else false").outputType, is(TypeSpec.BOOLEAN));

        assertThat(makeExpression("1 = 12").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1.0 = 12.0").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("true = false").outputType, is(TypeSpec.BOOLEAN));

        assertThat(makeExpression("1 <> 12").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1.0 <> 12.0").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("true <> false").outputType, is(TypeSpec.BOOLEAN));
    }

    @Test
    public void unopTest() {
        assertThat(makeExpression("+123").outputType, is(TypeSpec.INTEGER));
        assertThat(makeExpression("-123").outputType, is(TypeSpec.INTEGER));

        assertThat(makeExpression("+123.3124").outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("-1283471.1423").outputType, is(TypeSpec.REAL));

        assertThat(makeExpression("not true").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("not false").outputType, is(TypeSpec.BOOLEAN));
    }

    @Test
    public void complexExpressionTest() {
        assertThat(makeExpression("(1+13.0)<(12*1.6/53)").outputType, is(TypeSpec.BOOLEAN));
        assertThat(makeExpression("1+12+14*134-12 mod 31").outputType, is(TypeSpec.INTEGER));
        assertThat(makeExpression("(1 mod 31) / (231 div 123)").outputType, is(TypeSpec.REAL));
    }

    @Test
    public void wrongTypeBinOpTest() {
        // + - * / div mod not and or = <> < > <= >=
        doExpressionTypeFail("true + false");
        doExpressionTypeFail("true + 12");
        doExpressionTypeFail("141.14 + true");

        doExpressionTypeFail("true - false");
        doExpressionTypeFail("true - 12");
        doExpressionTypeFail("141.14 - true");

        doExpressionTypeFail("true * false");
        doExpressionTypeFail("true * 12");
        doExpressionTypeFail("141.14 * true");

        doExpressionTypeFail("14/true");
        doExpressionTypeFail("true/false");
        doExpressionTypeFail("false/412.14");

        doExpressionTypeFail("12.0 mod 41.12");
        doExpressionTypeFail("12 mod 41.12");
        doExpressionTypeFail("12.0 mod 41");
        doExpressionTypeFail("12 mod true");
        doExpressionTypeFail("false mod 3");

        doExpressionTypeFail("12.0 div 41.12");
        doExpressionTypeFail("12 div 41.12");
        doExpressionTypeFail("12.0 div 41");
        doExpressionTypeFail("12 div true");
        doExpressionTypeFail("false div 3");

        doExpressionTypeFail("not 13");
        doExpressionTypeFail("not 123.124");

        doExpressionTypeFail("21 and true");
        doExpressionTypeFail("false and 412");
        doExpressionTypeFail("true and 1241.134");
        doExpressionTypeFail("12.124 and false");

        doExpressionTypeFail("21 or true");
        doExpressionTypeFail("false or 412");
        doExpressionTypeFail("true or 1241.134");
        doExpressionTypeFail("12.124 or false");

        doExpressionTypeFail("21 and then true");
        doExpressionTypeFail("false and then 412");
        doExpressionTypeFail("true and then 1241.134");
        doExpressionTypeFail("12.124 and then false");

        doExpressionTypeFail("21 or else true");
        doExpressionTypeFail("false or else 412");
        doExpressionTypeFail("true or else 1241.134");
        doExpressionTypeFail("12.124 or else false");

        doExpressionTypeFail("13.142 = 123");
        doExpressionTypeFail("1424.413 = true");
        doExpressionTypeFail("124 = 14.134");
        doExpressionTypeFail("false = 142.143");
        doExpressionTypeFail("1234 = true");
        doExpressionTypeFail("false = 143");

        doExpressionTypeFail("13.142 <> 123");
        doExpressionTypeFail("1424.413 <> true");
        doExpressionTypeFail("124 <> 14.134");
        doExpressionTypeFail("false <> 142.143");
        doExpressionTypeFail("1234 <> true");
        doExpressionTypeFail("false <> 143");

        doExpressionTypeFail("1424.413 <= true");
        doExpressionTypeFail("false <= 142.143");
        doExpressionTypeFail("1234 <= true");
        doExpressionTypeFail("false <= 143");

        doExpressionTypeFail("1424.413 >= true");
        doExpressionTypeFail("false >= 142.143");
        doExpressionTypeFail("1234 >= true");
        doExpressionTypeFail("false >= 143");

        doExpressionTypeFail("1424.413 < true");
        doExpressionTypeFail("false > 142.143");
        doExpressionTypeFail("1234 > true");
        doExpressionTypeFail("false > 143");

        doExpressionTypeFail("1424.413 < true");
        doExpressionTypeFail("false < 142.143");
        doExpressionTypeFail("1234 < true");
        doExpressionTypeFail("false < 143");
    }

    @Test
    public void variableBasicTest() {
        String variableDeclarations = "program p; var a: integer; b: real; c: boolean; begin end.";
        Scope programScope = ROOT_SCOPE.makeChildScope(Token.ID("p"));

        assertThat(makeExpression("a", variableDeclarations, programScope).outputType, is(TypeSpec.INTEGER));
        assertThat(makeExpression("b", variableDeclarations, programScope).outputType, is(TypeSpec.REAL));
        assertThat(makeExpression("c", variableDeclarations, programScope).outputType, is(TypeSpec.BOOLEAN));

        assertThat(makeExpression("a+b", variableDeclarations, programScope).outputType, is(TypeSpec.REAL));
        doExpressionTypeFail("a+c", variableDeclarations, programScope);
    }

    @Test
    public void badForLoopTest() {
        String programText;

        // this one is all OK
        programText = "program p; var a: integer; b: real; begin for a:= 1 to 12 do b := 12.0; end.";
        checkProgram(programText, false);

        // this one is not OK
        programText = "program p; var a: integer; b: real; begin for b:= 1 to 12 do a := 12.0; end.";
        checkProgram(programText, true);
    }

    @Test
    public void badAssignStatementTest() {
        String programText;

        programText = "program p; var a: integer; begin a := 1.0 end.";
        checkProgram(programText, true);

        programText = "program p; var a: integer; begin a := true end.";
        checkProgram(programText, true);

        programText = "program p; var a: integer; begin a := 1 end.";
        checkProgram(programText, false);

        programText = "program p; var a: real; begin a := 1.0 end.";
        checkProgram(programText, false);

        programText = "program p; var a: real; begin a := 1 end.";
        checkProgram(programText, false);

        programText = "program p; var a: real; begin a := true end.";
        checkProgram(programText, true);

        programText = "program p; var a: boolean; begin a := 1.0 end.";
        checkProgram(programText, true);

        programText = "program p; var a: boolean; begin a := 1 end.";
        checkProgram(programText, true);

        programText = "program p; var a: boolean; begin a := true end.";
        checkProgram(programText, false);
    }
}
