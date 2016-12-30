package io.github.rodyamirov.pascal.visitor;

import io.github.rodyamirov.pascal.SymbolTable;
import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.tree.AndThenNode;
import io.github.rodyamirov.pascal.tree.AssignNode;
import io.github.rodyamirov.pascal.tree.BinOpNode;
import io.github.rodyamirov.pascal.tree.BlockNode;
import io.github.rodyamirov.pascal.tree.BooleanConstantNode;
import io.github.rodyamirov.pascal.tree.CompoundNode;
import io.github.rodyamirov.pascal.tree.DeclarationNode;
import io.github.rodyamirov.pascal.tree.IfStatementNode;
import io.github.rodyamirov.pascal.tree.IntConstantNode;
import io.github.rodyamirov.pascal.tree.NoOpNode;
import io.github.rodyamirov.pascal.tree.OrElseNode;
import io.github.rodyamirov.pascal.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.pascal.tree.ProgramNode;
import io.github.rodyamirov.pascal.tree.RealConstantNode;
import io.github.rodyamirov.pascal.tree.StatementNode;
import io.github.rodyamirov.pascal.tree.UnaryOpNode;
import io.github.rodyamirov.pascal.tree.VariableAssignNode;
import io.github.rodyamirov.pascal.tree.VariableDeclarationNode;
import io.github.rodyamirov.pascal.tree.VariableEvalNode;

/**
 * Created by richard.rast on 12/27/16.
 */
public class SymbolTableBuilder extends NodeVisitor {
    private final SymbolTable.Builder builder;

    public SymbolTableBuilder() {
        builder = SymbolTable.builder();
    }

    public SymbolTable build() {
        return builder.build();
    }

    @Override
    public void visit(ProgramNode programNode) {
        // just looks for variable declaration nodes ...
        programNode.blockNode.acceptVisit(this);
    }

    @Override
    public void visit(IfStatementNode ifStatementNode) {
        throw TODOException.make();
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
    public void visit(ProcedureDeclarationNode procedureDeclarationNode) {
        throw TODOException.make();
    }

    @Override
    public void visit(BlockNode blockNode) {
        blockNode.declarationNode.acceptVisit(this);
        blockNode.compoundNode.acceptVisit(this);
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        for (Token<String> id : variableDeclarationNode.variableIds) {
            builder.addSymbol(id, variableDeclarationNode.varType);
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
}
