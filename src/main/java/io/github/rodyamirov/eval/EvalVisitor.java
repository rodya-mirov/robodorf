package io.github.rodyamirov.eval;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.SymbolTable;
import io.github.rodyamirov.symbols.SymbolTableBuilder;
import io.github.rodyamirov.symbols.SymbolValue;
import io.github.rodyamirov.symbols.SymbolValueTable;
import io.github.rodyamirov.tree.AndThenNode;
import io.github.rodyamirov.tree.AssignNode;
import io.github.rodyamirov.tree.BinOpNode;
import io.github.rodyamirov.tree.BlockNode;
import io.github.rodyamirov.tree.BooleanConstantNode;
import io.github.rodyamirov.tree.CompoundNode;
import io.github.rodyamirov.tree.DeclarationNode;
import io.github.rodyamirov.tree.ExpressionNode;
import io.github.rodyamirov.tree.IfStatementNode;
import io.github.rodyamirov.tree.IntConstantNode;
import io.github.rodyamirov.tree.NoOpNode;
import io.github.rodyamirov.tree.NodeVisitor;
import io.github.rodyamirov.tree.OrElseNode;
import io.github.rodyamirov.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.tree.ProgramNode;
import io.github.rodyamirov.tree.RealConstantNode;
import io.github.rodyamirov.tree.StatementNode;
import io.github.rodyamirov.tree.UnaryOpNode;
import io.github.rodyamirov.tree.VariableAssignNode;
import io.github.rodyamirov.tree.VariableDeclarationNode;
import io.github.rodyamirov.tree.VariableEvalNode;
import io.github.rodyamirov.utils.TODOException;

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
    public void visit(AndThenNode andThenNode) {
        // short circuit evaluation of and
        // essentially: left ? right : left
        andThenNode.left.acceptVisit(this);
        SymbolValue<Boolean> leftResult = resultStack.pop();

        if (leftResult.value) {
            andThenNode.right.acceptVisit(this);
            SymbolValue<Boolean> rightResult = resultStack.pop();

            resultStack.push(rightResult);
        } else {
            resultStack.push(leftResult);
        }
    }

    @Override
    public void visit(OrElseNode orElseNode) {
        // short circuit evaluation of or
        // essentially: left ? left : right
        orElseNode.left.acceptVisit(this);
        SymbolValue<Boolean> leftResult = resultStack.pop();

        if (leftResult.value) {
            resultStack.push(leftResult);
        } else {
            orElseNode.right.acceptVisit(this);
            SymbolValue<Boolean> rightResult = resultStack.pop();

            resultStack.push(rightResult);
        }
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
    public void visit(IfStatementNode ifStatementNode) {
        ifStatementNode.condition.acceptVisit(this);
        SymbolValue<Boolean> conditionResult = resultStack.pop();

        if (conditionResult.value) {
            ifStatementNode.thenStatement.acceptVisit(this);
        } else {
            ifStatementNode.elseStatement.ifPresent(s -> s.acceptVisit(this));
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

        globals.setValue(assignNode.scope, varToken, result);
    }

    @Override
    public void visit(VariableAssignNode variableAssignNode) {
        // does nothing; symbol table handles this
    }

    @Override
    public void visit(VariableEvalNode variableEvalNode) {
        Token<String> varToken = variableEvalNode.idToken;
        SymbolValue result = globals.getValue(variableEvalNode.scope, varToken);

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
