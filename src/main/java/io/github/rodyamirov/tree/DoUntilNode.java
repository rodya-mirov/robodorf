package io.github.rodyamirov.tree;

import io.github.rodyamirov.symbols.Scope;

import java.util.Objects;

/**
 * Created by richard.rast on 12/30/16.
 */
public final class DoUntilNode extends LoopStatementNode {
    public final ExpressionNode condition;
    public final StatementNode childStatement;

    public DoUntilNode(Scope scope, ExpressionNode condition, StatementNode childStatement) {
        super(scope);
        this.condition = condition;
        this.childStatement = childStatement;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DoUntilNode)) {
            return false;
        }

        DoUntilNode other = (DoUntilNode)o;

        return Objects.equals(this.scope, other.scope)
                && Objects.equals(this.condition, this.condition)
                && Objects.equals(this.childStatement, other.childStatement);
    }

    @Override
    public int hashCode() {
        return 43*43*scope.hashCode() + 43*condition.hashCode() + childStatement.hashCode();
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
