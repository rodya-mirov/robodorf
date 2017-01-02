package io.github.rodyamirov.eval;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.Scope;
import io.github.rodyamirov.symbols.SymbolTable;
import io.github.rodyamirov.symbols.SymbolValue;
import io.github.rodyamirov.symbols.SymbolValueTable;
import io.github.rodyamirov.symbols.TypeSpec;
import io.github.rodyamirov.tree.AndThenNode;
import io.github.rodyamirov.tree.AssignNode;
import io.github.rodyamirov.tree.BinOpNode;
import io.github.rodyamirov.tree.BlockNode;
import io.github.rodyamirov.tree.BooleanConstantNode;
import io.github.rodyamirov.tree.CompoundNode;
import io.github.rodyamirov.tree.DeclarationNode;
import io.github.rodyamirov.tree.DoUntilNode;
import io.github.rodyamirov.tree.ExpressionNode;
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
import io.github.rodyamirov.tree.UnaryOpNode;
import io.github.rodyamirov.tree.VariableAssignNode;
import io.github.rodyamirov.tree.VariableDeclarationNode;
import io.github.rodyamirov.tree.VariableEvalNode;
import io.github.rodyamirov.tree.WhileNode;
import io.github.rodyamirov.utils.SingleElementStack;

import java.util.function.Supplier;

/**
 * Created by richard.rast on 12/25/16.
 */
public class EvalVisitor extends NodeVisitor {
    public static SymbolValueTable evaluateProgram(ProgramNode programNode, SymbolTable symbolTable) {
        EvalVisitor evalVisitor = new EvalVisitor(symbolTable);
        programNode.acceptVisit(evalVisitor);
        return evalVisitor.symbolValueTable;
    }

    public static SymbolValue evaluateExpression(ExpressionNode expressionNode, SymbolTable symbolTable) {
        EvalVisitor evalVisitor = new EvalVisitor(symbolTable);
        expressionNode.acceptVisit(evalVisitor);
        return evalVisitor.resultStack.pop();
    }

    private final SingleElementStack<SymbolValue> resultStack = new SingleElementStack<>();
    private final SingleElementStack<LoopControlNode> loopControlNodes = new SingleElementStack<>();

    private final SymbolValueTable symbolValueTable;

    private EvalVisitor(SymbolTable globalDeclarations) {
        symbolValueTable = new SymbolValueTable(globalDeclarations);
    }

    @Override
    public void visit(WhileNode whileNode) {
        Supplier<Boolean> checkCondition =
                () -> {
                    whileNode.condition.acceptVisit(this);
                    SymbolValue<Boolean> result = resultStack.pop();
                    return result.value;
                };

        while (checkCondition.get()) {
            whileNode.childStatement.acceptVisit(this);
            if (endLoopShouldBreak()) {
                break;
            }
        }
    }

    @Override
    public void visit(DoUntilNode doUntilNode) {
        Supplier<Boolean> checkCondition =
                () -> {
                    doUntilNode.condition.acceptVisit(this);
                    SymbolValue<Boolean> result = resultStack.pop();
                    return result.value;
                };

        do {
            doUntilNode.childStatement.acceptVisit(this);
            if (endLoopShouldBreak()) {
                break;
            }
        } while (! checkCondition.get());
    }

    @Override
    public void visit(ForNode forNode) {
        VariableAssignNode loopVariable = forNode.assignNode.variableAssignNode;

        // set up the start
        forNode.assignNode.expressionNode.acceptVisit(this);
        int start = ((SymbolValue<Integer>) resultStack.pop()).value;

        // set up the end
        forNode.bound.acceptVisit(this);
        int end = ((SymbolValue<Integer>) resultStack.pop()).value;

        int change;
        switch (forNode.direction) {
            case FORWARD:
                change = 1; break;

            case BACKWARD:
                change = -1; break;

            default:
                String message = String.format("Unrecognized direction %s for a for-loop", forNode.direction);
                throw new IllegalStateException(message);
        }

        for (int i = start; i != end+change; i += change) {
            SymbolValue<Integer> loopValue = SymbolValue.make(TypeSpec.INTEGER, i);
            symbolValueTable.setValue(forNode.scope, loopVariable.idToken, loopValue);
            symbolValueTable.lockValue(forNode.scope, loopVariable.idToken);

            forNode.body.acceptVisit(this);

            symbolValueTable.unlockValue(forNode.scope, loopVariable.idToken);

            if (endLoopShouldBreak()) {
                break;
            }
        }
    }

    // safely pops the top loop control directive; returns true iff it's a break
    private boolean endLoopShouldBreak() {
        if (loopControlNodes.isEmpty()) {
            return false;
        } else {
            LoopControlNode loopControlNode = loopControlNodes.pop();

            switch (loopControlNode.type) {
                case BREAK:
                    return true;
                case CONTINUE:
                    return false;

                default:
                    String message = String.format(
                            "Unrecognized loop control directive %s",
                            loopControlNode.type
                    );
                    throw new IllegalArgumentException(message);
            }
        }
    }

    @Override
    public void visit(LoopControlNode loopControlNode) {
        loopControlNodes.push(loopControlNode);
    }

    @Override
    public void visit(ProcedureCallNode procCall) {
        SymbolValue<ProcedureDeclarationNode> procValue =
                symbolValueTable.getValue(procCall.scope, procCall.procedureName);

        ProcedureDeclarationNode call = procValue.value;

        // just execute everything in the procedure declaration
        call.blockNode.acceptVisit(this);

        // then clear out instance variables! they should not persist between calls
        for (VariableDeclarationNode vdn : call.blockNode.declarationNode.variableDeclarations) {
            Scope scope = vdn.scope;
            for (Token<String> idToken : vdn.variableIds) {
                symbolValueTable.clearValue(scope, idToken);
            }
        }
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        // does nothing; this is handled by the builder which has already run
    }

    @Override
    public void visit(ProgramNode programNode) {
        symbolValueTable.setValue(
                programNode.scope,
                programNode.name,
                SymbolValue.make(TypeSpec.PROGRAM, programNode)
        );

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
        symbolValueTable.setValue(
                procedureDeclarationNode.scope,
                procedureDeclarationNode.name,
                SymbolValue.make(TypeSpec.PROCEDURE, procedureDeclarationNode)
        );
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

        symbolValueTable.setValue(assignNode.scope, varToken, result);
    }

    @Override
    public void visit(VariableAssignNode variableAssignNode) {
        // does nothing; symbol table handles this
    }

    @Override
    public void visit(VariableEvalNode variableEvalNode) {
        Token<String> varToken = variableEvalNode.idToken;
        SymbolValue result = symbolValueTable.getValue(variableEvalNode.scope, varToken);

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
            // this is the only place a continue/break actually does anything
            if (! loopControlNodes.isEmpty()){
                break;
            }

            statement.acceptVisit(this);
        }
    }

    @Override
    public void visit(NoOpNode noOpNode) {
        // it's a No Op
        // that means _no operations_
    }

    @Override
    public void visit(UnaryOpNode unaryOpNode) {
        unaryOpNode.child.acceptVisit(this);
        SymbolValue childValue = resultStack.pop();

        SymbolValue value = unaryOpNode.function.apply(childValue);
        resultStack.push(value);
    }
}
