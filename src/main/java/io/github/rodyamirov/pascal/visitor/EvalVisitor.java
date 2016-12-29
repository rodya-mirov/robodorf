package io.github.rodyamirov.pascal.visitor;

import io.github.rodyamirov.pascal.SymbolTable;
import io.github.rodyamirov.pascal.SymbolValue;
import io.github.rodyamirov.pascal.SymbolValueTable;
import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.tree.AssignNode;
import io.github.rodyamirov.pascal.tree.BinOpNode;
import io.github.rodyamirov.pascal.tree.BlockNode;
import io.github.rodyamirov.pascal.tree.BooleanConstantNode;
import io.github.rodyamirov.pascal.tree.CompoundNode;
import io.github.rodyamirov.pascal.tree.DeclarationNode;
import io.github.rodyamirov.pascal.tree.ExpressionNode;
import io.github.rodyamirov.pascal.tree.IntConstantNode;
import io.github.rodyamirov.pascal.tree.NoOpNode;
import io.github.rodyamirov.pascal.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.pascal.tree.ProgramNode;
import io.github.rodyamirov.pascal.tree.RealConstantNode;
import io.github.rodyamirov.pascal.tree.StatementNode;
import io.github.rodyamirov.pascal.tree.UnaryOpNode;
import io.github.rodyamirov.pascal.tree.VariableAssignNode;
import io.github.rodyamirov.pascal.tree.VariableDeclarationNode;
import io.github.rodyamirov.pascal.tree.VariableEvalNode;

import java.util.Stack;

/**
 * Created by richard.rast on 12/25/16.
 */
public class EvalVisitor extends NodeVisitor {
    public static SymbolValue evaluateExpression(ExpressionNode expressionNode, SymbolTable symbolTable) {
        EvalVisitor visitor = new EvalVisitor(symbolTable);
        expressionNode.acceptVisit(visitor);
        return visitor.resultStack.pop();
    }

    public static SymbolValueTable evaluateProgram(ProgramNode programNode) {
        SymbolTableBuilder stb = new SymbolTableBuilder();
        programNode.acceptVisit(stb);
        SymbolTable symbolTable = stb.build();

        EvalVisitor visitor = new EvalVisitor(symbolTable);
        programNode.acceptVisit(visitor);
        return visitor.globals;
    }

    private final Stack<SymbolValue> resultStack = new Stack<>();
    private final SymbolValueTable globals;

    private EvalVisitor(SymbolTable globalDeclarations) {
        globals = new SymbolValueTable(globalDeclarations);
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        // does nothing; this is handled by the builder which has already run
    }

    @Override
    public void visit(ProgramNode programNode) {
        // TODO: use the programNode.name to make scopes
        programNode.blockNode.acceptVisit(this);
    }

    @Override
    public void visit(BlockNode blockNode) {
        blockNode.declarationNode.acceptVisit(this);
        blockNode.compoundNode.acceptVisit(this);
    }

    @Override
    public void visit(DeclarationNode declarationNode) {
        for (VariableDeclarationNode vdn : declarationNode.variableDeclarations) {
            vdn.acceptVisit(this);
        }

        for (ProcedureDeclarationNode pdn : declarationNode.procedureDeclarations) {
            pdn.acceptVisit(this);
        }
    }

    @Override
    public void visit(ProcedureDeclarationNode procedureDeclarationNode) {
        throw TODOException.make();
    }

    @Override
    public void visit(RealConstantNode realConstantNode) {
        resultStack.push(realConstantNode.value);
    }

    @Override
    public void visit(IntConstantNode constantNode) {
        resultStack.push(constantNode.value);
    }

    @Override
    public void visit(BooleanConstantNode constantNode) {
        resultStack.push(constantNode.value);
    }

    @Override
    public void visit(AssignNode assignNode) {
        assignNode.variableAssignNode.acceptVisit(this);
        Token<String> varToken = assignNode.variableAssignNode.idToken;

        // now figure out what to set the variable to ...
        assignNode.expressionNode.acceptVisit(this);
        SymbolValue result = resultStack.pop();

        globals.setValue(varToken, result);
    }

    @Override
    public void visit(VariableAssignNode variableAssignNode) {
        // does nothing; symbol table handles this
    }

    @Override
    public void visit(VariableEvalNode variableEvalNode) {
        Token<String> varToken = variableEvalNode.idToken;
        SymbolValue result = globals.getValue(varToken);

        resultStack.push(result);
    }

    @Override
    public void visit(BinOpNode binOpNode) {
        binOpNode.left.acceptVisit(this);
        SymbolValue left = resultStack.pop();

        binOpNode.right.acceptVisit(this);
        SymbolValue right = resultStack.pop();

        SymbolValue result = binOpNode.function.apply(left, right);

        resultStack.push(result);
    }

    @Override
    public void visit(CompoundNode compoundNode) {
        for (StatementNode statement : compoundNode.statements) {
            statement.acceptVisit(this);
        }
    }

    @Override
    public void visit(NoOpNode noOpNode) {
        // it's a No Op
        // that means _no_ _operations_
    }

    @Override
    public void visit(UnaryOpNode unaryOpNode) {
        unaryOpNode.child.acceptVisit(this);
        SymbolValue childValue = resultStack.pop();

        SymbolValue value = unaryOpNode.function.apply(childValue);
        resultStack.push(value);
    }
}
