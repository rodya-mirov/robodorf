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
import io.github.rodyamirov.tree.UnaryOpNode;
import io.github.rodyamirov.tree.VariableAssignNode;
import io.github.rodyamirov.tree.VariableDeclarationNode;
import io.github.rodyamirov.tree.VariableEvalNode;
import io.github.rodyamirov.tree.WhileNode;

/**
 * Created by richard.rast on 12/27/16.
 */
public class SymbolTableBuilder extends NodeVisitor {
    private final SymbolTable.Builder builder;

    public static SymbolTable buildFrom(ProgramNode programNode) {
        SymbolTableBuilder visitor = new SymbolTableBuilder();
        programNode.acceptVisit(visitor);
        return visitor.build();
    }

    public SymbolTableBuilder() {
        builder = SymbolTable.builder();
    }

    public SymbolTable build() {
        return builder.build();
    }

    @Override
    public void visit(ProgramNode programNode) {
        builder.addSymbol(programNode.scope, programNode.name, TypeSpec.PROGRAM);
        programNode.blockNode.acceptVisit(this);
    }

    @Override
    public void visit(IfStatementNode ifStatementNode) {
        ifStatementNode.condition.acceptVisit(this);
        ifStatementNode.thenStatement.acceptVisit(this);
        ifStatementNode.elseStatement.ifPresent(s -> s.acceptVisit(this));
    }

    @Override
    public void visit(DeclarationNode declarationNode) {
        for (VariableDeclarationNode v : declarationNode.variableDeclarations) {
            v.acceptVisit(this);
        }

        for (ProcedureDeclarationNode p : declarationNode.procedureDeclarations) {
            p.acceptVisit(this);
        }
    }

    @Override
    public void visit(ProcedureDeclarationNode procDecNode) {
        builder.addSymbol(procDecNode.scope, procDecNode.name, TypeSpec.PROCEDURE);
        procDecNode.blockNode.acceptVisit(this);
    }

    @Override
    public void visit(BlockNode blockNode) {
        blockNode.declarationNode.acceptVisit(this);
        blockNode.compoundNode.acceptVisit(this);
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        Scope scope = variableDeclarationNode.scope;
        TypeSpec typeSpec = variableDeclarationNode.varType;

        for (Token<String> id : variableDeclarationNode.variableIds) {
            builder.addSymbol(scope, id, typeSpec);
        }
    }

    @Override
    public void visit(AssignNode assignNode) {
        assignNode.variableAssignNode.acceptVisit(this);
        assignNode.expressionNode.acceptVisit(this);
    }

    @Override
    public void visit(BinOpNode binOpNode) {
        binOpNode.left.acceptVisit(this);
        binOpNode.right.acceptVisit(this);
    }

    @Override
    public void visit(CompoundNode compoundNode) {
        for (StatementNode statementNode : compoundNode.statements) {
            statementNode.acceptVisit(this);
        }
    }

    @Override
    public void visit(AndThenNode andThenNode) {
        andThenNode.left.acceptVisit(this);
        andThenNode.right.acceptVisit(this);
    }

    @Override
    public void visit(OrElseNode orElseNode) {
        orElseNode.left.acceptVisit(this);
        orElseNode.right.acceptVisit(this);
    }

    @Override
    public void visit(IntConstantNode intConstantNode) {
        // does nothing; we're only concerned with variable declarations
    }

    @Override
    public void visit(BooleanConstantNode booleanConstantNode) {
        // does nothing; we're only concerned with variable declarations
    }

    @Override
    public void visit(NoOpNode noOpNode) {
        // does nothing; we're only concerned with variable declarations
    }

    @Override
    public void visit(RealConstantNode intConstantNode) {
        // does nothing; we're only concerned with variable declarations
    }

    @Override
    public void visit(UnaryOpNode unaryOpNode) {
        unaryOpNode.child.acceptVisit(this);
    }

    @Override
    public void visit(VariableAssignNode variableAssignNode) {
        // does nothing; we're only concerned with variable declarations
    }

    @Override
    public void visit(VariableEvalNode variableEvalNode) {
        // does nothing; we're only concerned with variable declarations
    }

    @Override
    public void visit(ProcedureCallNode procedureCallNode) {
        // does nothing; we're only concerned with variable declarations
    }

    @Override
    public void visit(LoopControlNode loopControlNode) {
        // does nothing; we're only concerned with variable declarations
    }

    @Override
    public void visit(WhileNode whileNode) {
        whileNode.condition.acceptVisit(this);
        whileNode.childStatement.acceptVisit(this);
    }

    @Override
    public void visit(DoUntilNode doUntilNode) {
        doUntilNode.condition.acceptVisit(this);
        doUntilNode.childStatement.acceptVisit(this);
    }
}
