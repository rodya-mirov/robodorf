package io.github.rodyamirov.tree;

import io.github.rodyamirov.symbols.Scope;

import java.util.Objects;

/**
 * Created by richard.rast on 12/30/16.
 */
public final class DoUntilNode extends LoopStatementNode {
    public final ExpressionNode condition;
    public final StatementNode childStatement;

    public DoUntilNode(ExpressionNode condition, StatementNode childStatement) {
        this.condition = condition;
        this.childStatement = childStatement;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DoUntilNode)) {
            return false;
        }

        DoUntilNode other = (DoUntilNode)o;

        return Objects.equals(this.condition, this.condition)
                && Objects.equals(this.childStatement, other.childStatement)
                && Objects.equals(this.scope, other.scope);
    }

    @Override
    public int hashCode() {
        int out = Objects.hashCode(scope);
        out = 43 * out + condition.hashCode();
        out = 43 * out + childStatement.hashCode();
        return out;
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
