package io.github.rodyamirov.pascal.tree;

import io.github.rodyamirov.pascal.visitor.NodeVisitor;

import java.util.Objects;

/**
 * Created by richard.rast on 12/22/16.
 */
public final class AssignNode extends StatementNode {
    public final VariableAssignNode variableAssignNode;
    public final ExpressionNode expressionNode;

    public AssignNode(VariableAssignNode variableAssignNode, ExpressionNode expressionNode) {
        this.variableAssignNode = variableAssignNode;
        this.expressionNode = expressionNode;
    }

    @Override
    public void acceptVisit(NodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof AssignNode)) {
            return false;
        }

        AssignNode other = (AssignNode)o;

        return Objects.equals(this.variableAssignNode, other.variableAssignNode)
                && Objects.equals(this.expressionNode, other.expressionNode);
    }
}
