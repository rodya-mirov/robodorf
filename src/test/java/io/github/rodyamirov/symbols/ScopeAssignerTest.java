package io.github.rodyamirov.symbols;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.parse.Parser;
import io.github.rodyamirov.tree.AndThenNode;
import io.github.rodyamirov.tree.AssignNode;
import io.github.rodyamirov.tree.BinOpNode;
import io.github.rodyamirov.tree.BlockNode;
import io.github.rodyamirov.tree.BooleanConstantNode;
import io.github.rodyamirov.tree.CompoundNode;
import io.github.rodyamirov.tree.DeclarationNode;
import io.github.rodyamirov.tree.DoUntilNode;
import io.github.rodyamirov.tree.ForNode;
import io.github.rodyamirov.tree.IfStatementNode;
import io.github.rodyamirov.tree.IntConstantNode;
import io.github.rodyamirov.tree.LoopControlNode;
import io.github.rodyamirov.tree.NoOpNode;
import io.github.rodyamirov.tree.OrElseNode;
import io.github.rodyamirov.tree.ProcedureCallNode;
import io.github.rodyamirov.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.tree.ProgramNode;
import io.github.rodyamirov.tree.RealConstantNode;
import io.github.rodyamirov.tree.StatementNode;
import io.github.rodyamirov.tree.UnaryOpNode;
import io.github.rodyamirov.tree.VariableAssignNode;
import io.github.rodyamirov.tree.VariableDeclarationNode;
import io.github.rodyamirov.tree.VariableEvalNode;
import io.github.rodyamirov.tree.WhileNode;
import org.junit.Test;

import java.util.Optional;

import static io.github.rodyamirov.symbols.ScopeAssigner.ROOT_SCOPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 1/1/17.
 */
public class ScopeAssignerTest {
    @Test
    public void constantsTest() {
        assertThat(ROOT_SCOPE, is(Scope.makeRootScope(Token.ID("root"))));
        assertThat(ROOT_SCOPE.immediateScopeName, is(Token.ID("root")));
        assertThat(ROOT_SCOPE.parentScope, is(Optional.empty()));
    }

    @Test
    public void andThenNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("hey")).makeChildScope(Token.ID("idk"));
        AndThenNode andThenNode = (AndThenNode) Parser.parseExpression("true and then false");
        ScopeAssigner.assignScopes(testScope, andThenNode);

        assertThat(andThenNode.scope, is(testScope));
        assertThat(andThenNode.left.scope, is(testScope));
        assertThat(andThenNode.right.scope, is(testScope));
    }

    @Test
    public void assignNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("assignNodeTest"));
        AssignNode assignNode = (AssignNode) Parser.parseStatement("a := 3");
        ScopeAssigner.assignScopes(testScope, assignNode);

        assertThat(assignNode.scope, is(testScope));
        assertThat(assignNode.variableAssignNode.scope, is(testScope));
        assertThat(assignNode.expressionNode.scope, is(testScope));
    }

    @Test
    public void binOpNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("hello"));
        BinOpNode binOpNode = (BinOpNode) Parser.parseExpression("1+2+3");
        ScopeAssigner.assignScopes(testScope, binOpNode);

        assertThat(binOpNode.scope, is(testScope));
        assertThat(binOpNode.left.scope, is(testScope));
        assertThat(binOpNode.right.scope, is(testScope));
    }

    @Test
    public void blockNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("dsnkdn"));
        BlockNode blockNode = Parser.parseProgram("program as; begin end.").blockNode;
        ScopeAssigner.assignScopes(testScope, blockNode);

        assertThat(blockNode.scope, is(testScope));
        assertThat(blockNode.compoundNode.scope, is(testScope));
        assertThat(blockNode.declarationNode.scope, is(testScope));
    }

    @Test
    public void booleanConstantNode() {
        Scope testScope = ROOT_SCOPE;
        BooleanConstantNode booleanConstantNode = (BooleanConstantNode) Parser.parseExpression("true");
        ScopeAssigner.assignScopes(testScope, booleanConstantNode);

        assertThat(booleanConstantNode.scope, is(testScope));
    }

    @Test
    public void compoundNode() {
        Scope testScope = ROOT_SCOPE;
        CompoundNode compoundNode = (CompoundNode) Parser.parseStatement("begin a := 1; b := 2; end");
        ScopeAssigner.assignScopes(testScope, compoundNode);

        assertThat(compoundNode.scope, is(testScope));

        for (StatementNode statementNode : compoundNode.statements) {
            assertThat(statementNode.scope, is(testScope));
        }
    }

    @Test
    public void declarationNode() {
        Scope testScope = ROOT_SCOPE;
        DeclarationNode declarationNode = Parser.parseProgram("program a; var b: integer; procedure test; begin end; begin end.").blockNode.declarationNode;
        ScopeAssigner.assignScopes(testScope, declarationNode);

        assertThat(declarationNode.scope, is(testScope));

        for (VariableDeclarationNode vdn : declarationNode.variableDeclarations) {
            assertThat(vdn.scope, is(testScope));
        }

        for (ProcedureDeclarationNode pdn : declarationNode.procedureDeclarations) {
            assertThat(pdn.scope, is(testScope));
        }
    }

    @Test
    public void doUntilNode() {
        Scope testScope = ROOT_SCOPE;
        DoUntilNode doUntilNode = (DoUntilNode) Parser.parseStatement("do a := 1 until true");
        ScopeAssigner.assignScopes(testScope, doUntilNode);

        assertThat(doUntilNode.scope, is(testScope));
        assertThat(doUntilNode.condition.scope, is(testScope));
        assertThat(doUntilNode.childStatement.scope, is(testScope));
    }

    @Test
    public void forNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("djsnjdns"));

        ForNode forNode = (ForNode) Parser.parseStatement("for a := 1 to 12 do b := 3");
        ScopeAssigner.assignScopes(testScope, forNode);

        assertThat(forNode.scope, is(testScope));
        assertThat(forNode.assignNode.scope, is(testScope));
        assertThat(forNode.body.scope, is(testScope));
        assertThat(forNode.bound.scope, is(testScope));

        forNode = (ForNode) Parser.parseStatement("for a:=10 downto 3 do b:=1");
        ScopeAssigner.assignScopes(testScope, forNode);

        assertThat(forNode.scope, is(testScope));
        assertThat(forNode.assignNode.scope, is(testScope));
        assertThat(forNode.body.scope, is(testScope));
        assertThat(forNode.bound.scope, is(testScope));
    }

    @Test
    public void ifNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("dsjnd")).makeChildScope(Token.ID("dsnd"));

        IfStatementNode ifStatementNode = (IfStatementNode) Parser.parseStatement("if true then a:= 1 else a := 2");
        ScopeAssigner.assignScopes(testScope, ifStatementNode);

        assertThat(ifStatementNode.scope, is(testScope));
        assertThat(ifStatementNode.condition.scope, is(testScope));
        assertThat(ifStatementNode.thenStatement.scope, is(testScope));
        assertThat(ifStatementNode.elseStatement.get().scope, is(testScope));

        ifStatementNode = (IfStatementNode) Parser.parseStatement("if true then a:= 1");
        ScopeAssigner.assignScopes(testScope, ifStatementNode);

        assertThat(ifStatementNode.scope, is(testScope));
        assertThat(ifStatementNode.condition.scope, is(testScope));
        assertThat(ifStatementNode.thenStatement.scope, is(testScope));
        assertThat(ifStatementNode.elseStatement.isPresent(), is(false));
    }

    @Test
    public void intConstantNode() {
        Scope testScope = ROOT_SCOPE;

        IntConstantNode intConstantNode = (IntConstantNode) Parser.parseExpression("1231");
        ScopeAssigner.assignScopes(testScope, intConstantNode);

        assertThat(intConstantNode.scope, is(testScope));
    }

    @Test
    public void loopControlNode() {
        Scope testScope = ROOT_SCOPE;

        LoopControlNode loopControlNode = (LoopControlNode) Parser.parseStatement("continue");
        ScopeAssigner.assignScopes(testScope, loopControlNode);

        assertThat(loopControlNode.scope, is(testScope));

        loopControlNode = (LoopControlNode) Parser.parseStatement("break");
        ScopeAssigner.assignScopes(testScope, loopControlNode);
    }

    @Test
    public void noopNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("dsknd"));

        NoOpNode noOpNode = (NoOpNode) Parser.parseStatement("");
        ScopeAssigner.assignScopes(testScope, noOpNode);

        assertThat(noOpNode.scope, is(testScope));
    }

    @Test
    public void orElseNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("skdn"));

        OrElseNode orElseNode = (OrElseNode) Parser.parseExpression("12 or else 13");
        ScopeAssigner.assignScopes(testScope, orElseNode);

        assertThat(orElseNode.scope, is(testScope));
        assertThat(orElseNode.left.scope, is(testScope));
        assertThat(orElseNode.right.scope, is(testScope));
    }

    @Test
    public void procedureCallNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("ajsananj"));

        ProcedureCallNode procedureCallNode = (ProcedureCallNode) Parser.parseStatement("f()");
        ScopeAssigner.assignScopes(testScope, procedureCallNode);

        assertThat(procedureCallNode.scope, is(testScope));
    }

    @Test
    public void procedureDeclarationNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("bottom"));

        ProcedureDeclarationNode procedureDeclarationNode = Parser
                .parseProgram("program a; procedure proc; var eh: real; begin end; begin end.")
                .blockNode.declarationNode.procedureDeclarations.get(0);

        ScopeAssigner.assignScopes(testScope, procedureDeclarationNode);

        assertThat(procedureDeclarationNode.scope, is(testScope));
        assertThat(procedureDeclarationNode.blockNode.scope, is(testScope.makeChildScope(Token.ID("proc"))));
    }

    @Test
    public void programNode() {
        Scope testScope = ROOT_SCOPE;

        ProgramNode programNode = Parser.parseProgram("program eh; begin end.");
        ScopeAssigner.assignScopes(testScope, programNode);

        assertThat(programNode.scope, is(testScope));
        assertThat(programNode.blockNode.scope, is(testScope.makeChildScope(Token.ID("eh"))));
        assertThat(programNode.blockNode.declarationNode.scope, is(testScope.makeChildScope(Token.ID("eh"))));
        assertThat(programNode.blockNode.compoundNode.scope, is(testScope.makeChildScope(Token.ID("eh"))));
    }

    @Test
    public void realConstantNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("andjsa"));

        RealConstantNode realConstantNode = (RealConstantNode) Parser.parseExpression("12.012");
        ScopeAssigner.assignScopes(testScope, realConstantNode);

        assertThat(realConstantNode.scope, is(testScope));
    }

    @Test
    public void unaryOpNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("dsknfksn"));

        UnaryOpNode unaryOpNode = (UnaryOpNode) Parser.parseExpression("+-+-12");
        ScopeAssigner.assignScopes(testScope, unaryOpNode);

        assertThat(unaryOpNode.scope, is(testScope));
        assertThat(unaryOpNode.child.scope, is(testScope));
    }

    @Test
    public void variableAssignNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("jdksnkd"));

        VariableAssignNode variableAssignNode = ((AssignNode) Parser.parseStatement("a := 1")).variableAssignNode;
        ScopeAssigner.assignScopes(testScope, variableAssignNode);

        assertThat(variableAssignNode.scope, is(testScope));
    }

    @Test
    public void variableDeclarationNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("sdsn"));

        VariableDeclarationNode variableDeclarationNode = Parser
                .parseProgram("program a; var b: integer; begin end .")
                .blockNode.declarationNode.variableDeclarations.get(0);
        ScopeAssigner.assignScopes(testScope, variableDeclarationNode);

        assertThat(variableDeclarationNode.scope, is(testScope));
    }

    @Test
    public void variableEvalNode() {
        Scope testScope = ROOT_SCOPE;

        VariableEvalNode variableEvalNode = (VariableEvalNode) Parser.parseExpression("a_A");
        ScopeAssigner.assignScopes(testScope, variableEvalNode);

        assertThat(variableEvalNode.scope, is(testScope));
    }

    @Test
    public void whileNode() {
        Scope testScope = Scope.makeRootScope(Token.ID("dsndn"));

        WhileNode whileNode = (WhileNode) Parser.parseStatement("while true do a := 12");
        ScopeAssigner.assignScopes(testScope, whileNode);

        assertThat(whileNode.scope, is(testScope));
        assertThat(whileNode.condition.scope, is(testScope));
        assertThat(whileNode.childStatement.scope, is(testScope));
    }
}
