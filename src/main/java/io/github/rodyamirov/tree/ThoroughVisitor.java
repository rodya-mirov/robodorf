package io.github.rodyamirov.tree;

/**
 * This is an essentially blank nodevisitor, made for when a new nodevisitor only needs to actually
 * do anything for a small number of types of nodes, but which needs to walk the entire tree.
 * Extending classes can override the few number of classes where specific behavior is needed
 * and leave the rest alone.
 *
 * Created by richard.rast on 1/2/17.
 */
public abstract class ThoroughVisitor extends NodeVisitor {

    @Override
    public void visit(AndThenNode andThenNode) {
        andThenNode.left.acceptVisit(this);
        andThenNode.right.acceptVisit(this);
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
    public void visit(BlockNode blockNode) {
        blockNode.declarationNode.acceptVisit(this);
        blockNode.compoundNode.acceptVisit(this);
    }

    @Override
    public void visit(BooleanConstantNode booleanConstantNode) {
    }

    @Override
    public void visit(CompoundNode compoundNode) {
        for (StatementNode statementNode : compoundNode.statements) {
            statementNode.acceptVisit(this);
        }
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
    public void visit(DoUntilNode doUntilNode) {
        doUntilNode.condition.acceptVisit(this);
        doUntilNode.childStatement.acceptVisit(this);
    }

    @Override
    public void visit(ForNode forNode) {
        forNode.assignNode.acceptVisit(this);
        forNode.body.acceptVisit(this);
        forNode.bound.acceptVisit(this);
    }

    @Override
    public void visit(IfStatementNode ifStatementNode) {
        ifStatementNode.condition.acceptVisit(this);
        ifStatementNode.thenStatement.acceptVisit(this);
        ifStatementNode.elseStatement.ifPresent(es -> es.acceptVisit(this));
    }

    @Override
    public void visit(IntConstantNode intConstantNode) {
    }

    @Override
    public void visit(LoopControlNode loopControlNode) {
    }

    @Override
    public void visit(NoOpNode noOpNode) {
    }

    @Override
    public void visit(OrElseNode orElseNode) {
        orElseNode.left.acceptVisit(this);
        orElseNode.right.acceptVisit(this);
    }

    @Override
    public void visit(ProcedureCallNode procedureCallNode) {
    }

    @Override
    public void visit(ProcedureDeclarationNode procedureDeclarationNode) {
        procedureDeclarationNode.blockNode.acceptVisit(this);
    }

    @Override
    public void visit(ProgramNode programNode) {
        programNode.blockNode.acceptVisit(this);
    }

    @Override
    public void visit(RealConstantNode realConstantNode) {
    }

    @Override
    public void visit(UnaryOpNode unaryOpNode) {
        unaryOpNode.child.acceptVisit(this);
    }

    @Override
    public void visit(VariableAssignNode variableAssignNode) {
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
    }

    @Override
    public void visit(VariableEvalNode variableEvalNode) {
    }

    @Override
    public void visit(WhileNode whileNode) {
        whileNode.condition.acceptVisit(this);
        whileNode.childStatement.acceptVisit(this);
    }
}
