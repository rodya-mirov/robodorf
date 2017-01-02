package io.github.rodyamirov.symbols;

import io.github.rodyamirov.lex.Token;
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
import io.github.rodyamirov.tree.NodeVisitor;
import io.github.rodyamirov.tree.OrElseNode;
import io.github.rodyamirov.tree.ProcedureCallNode;
import io.github.rodyamirov.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.tree.ProgramNode;
import io.github.rodyamirov.tree.RealConstantNode;
import io.github.rodyamirov.tree.StatementNode;
import io.github.rodyamirov.tree.SyntaxTree;
import io.github.rodyamirov.tree.UnaryOpNode;
import io.github.rodyamirov.tree.VariableAssignNode;
import io.github.rodyamirov.tree.VariableDeclarationNode;
import io.github.rodyamirov.tree.VariableEvalNode;
import io.github.rodyamirov.tree.WhileNode;

/**
 * Created by richard.rast on 1/1/17.
 */
public class ScopeAssigner extends NodeVisitor {
    // just a standard root scope for general use
    public static final Scope ROOT_SCOPE = Scope.makeRootScope(Token.ID("root"));

    private Scope currentScope;
    
    public ScopeAssigner(Scope scope) {
        this.currentScope = scope;
    }
    
    public static <T extends SyntaxTree> void assignScopes(Scope rootScope, T syntaxTree) {
        ScopeAssigner scopeAssigner = new ScopeAssigner(rootScope);
        syntaxTree.acceptVisit(scopeAssigner);
    }

    @Override
    public void visit(AndThenNode andThenNode) {
        andThenNode.scope = currentScope;

        andThenNode.left.acceptVisit(this);
        andThenNode.right.acceptVisit(this);
    }

    @Override
    public void visit(AssignNode assignNode) {
        assignNode.scope = currentScope;

        assignNode.variableAssignNode.acceptVisit(this);
        assignNode.expressionNode.acceptVisit(this);
    }

    @Override
    public void visit(BinOpNode binOpNode) {
        binOpNode.scope = currentScope;

        binOpNode.left.acceptVisit(this);
        binOpNode.right.acceptVisit(this);
    }

    @Override
    public void visit(BlockNode blockNode) {
        blockNode.scope = currentScope;

        blockNode.declarationNode.acceptVisit(this);
        blockNode.compoundNode.acceptVisit(this);
    }

    @Override
    public void visit(BooleanConstantNode booleanConstantNode) {
        booleanConstantNode.scope = currentScope;
    }

    @Override
    public void visit(CompoundNode compoundNode) {
        compoundNode.scope = currentScope;

        for (StatementNode statementNode : compoundNode.statements) {
            statementNode.acceptVisit(this);
        }
    }

    @Override
    public void visit(DeclarationNode declarationNode) {
        declarationNode.scope = currentScope;

        for (VariableDeclarationNode vdn : declarationNode.variableDeclarations) {
            vdn.acceptVisit(this);
        }

        for (ProcedureDeclarationNode pdn : declarationNode.procedureDeclarations) {
            pdn.acceptVisit(this);
        }
    }

    @Override
    public void visit(DoUntilNode doUntilNode) {
        doUntilNode.scope = currentScope;

        doUntilNode.condition.acceptVisit(this);
        doUntilNode.childStatement.acceptVisit(this);
    }

    @Override
    public void visit(ForNode forNode) {
        forNode.scope = currentScope;

        forNode.assignNode.acceptVisit(this);
        forNode.body.acceptVisit(this);
        forNode.bound.acceptVisit(this);
    }

    @Override
    public void visit(IfStatementNode ifStatementNode) {
        ifStatementNode.scope = currentScope;

        ifStatementNode.condition.acceptVisit(this);
        ifStatementNode.thenStatement.acceptVisit(this);
        ifStatementNode.elseStatement.ifPresent(es -> es.acceptVisit(this));
    }

    @Override
    public void visit(IntConstantNode intConstantNode) {
        intConstantNode.scope = currentScope;
    }

    @Override
    public void visit(LoopControlNode loopControlNode) {
        loopControlNode.scope = currentScope;
    }

    @Override
    public void visit(NoOpNode noOpNode) {
        noOpNode.scope = currentScope;
    }

    @Override
    public void visit(OrElseNode orElseNode) {
        orElseNode.scope = currentScope;

        orElseNode.left.acceptVisit(this);
        orElseNode.right.acceptVisit(this);
    }

    @Override
    public void visit(ProcedureCallNode procedureCallNode) {
        procedureCallNode.scope = currentScope;
    }

    @Override
    public void visit(ProcedureDeclarationNode procedureDeclarationNode) {
        procedureDeclarationNode.scope = currentScope;

        Scope oldScope = currentScope;
        currentScope = currentScope.makeChildScope(procedureDeclarationNode.name);

        procedureDeclarationNode.blockNode.acceptVisit(this);

        currentScope = oldScope;
    }

    @Override
    public void visit(ProgramNode programNode) {
        programNode.scope = currentScope;

        Scope oldScope = currentScope;
        currentScope = currentScope.makeChildScope(programNode.name);

        programNode.blockNode.acceptVisit(this);

        currentScope = oldScope;
    }

    @Override
    public void visit(RealConstantNode intConstantNode) {
        intConstantNode.scope = currentScope;
    }

    @Override
    public void visit(UnaryOpNode unaryOpNode) {
        unaryOpNode.scope = currentScope;

        unaryOpNode.child.acceptVisit(this);
    }

    @Override
    public void visit(VariableAssignNode variableAssignNode) {
        variableAssignNode.scope = currentScope;
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        variableDeclarationNode.scope = currentScope;
    }

    @Override
    public void visit(VariableEvalNode variableEvalNode) {
        variableEvalNode.scope = currentScope;
    }

    @Override
    public void visit(WhileNode whileNode) {
        whileNode.scope = currentScope;

        whileNode.condition.acceptVisit(this);
        whileNode.childStatement.acceptVisit(this);
    }
}
