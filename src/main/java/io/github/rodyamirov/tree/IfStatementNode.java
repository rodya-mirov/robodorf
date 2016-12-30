package io.github.rodyamirov.tree;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an IF-statement. The ELSE-part is optional.
 * Note that despite what various internet sites state, the following is true from the ISO spec:
 *  (1) there is nothing special about "else if" as a construction; it's just else [statement]
 *      where the [statement] could actually be an "if" statement
 *  (2) there is NO semicolon needed after the construction (bringing it in line with everything
 *      else in the language; semicolons are only needed to separate statements in a list)
 *
 * Created by richard.rast on 12/29/16.
 */
public final class IfStatementNode extends StatementNode {
    public final ExpressionNode condition;
    public final StatementNode thenStatement;
    public final Optional<StatementNode> elseStatement;

    public IfStatementNode(ExpressionNode condition, StatementNode thenStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = Optional.empty();
    }

    public IfStatementNode(ExpressionNode condition, StatementNode thenStatement,
            StatementNode elseStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = Optional.of(elseStatement);
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof IfStatementNode)) {
            return false;
        }

        IfStatementNode other = (IfStatementNode)o;
        return Objects.equals(this.condition, other.condition)
                && Objects.equals(this.thenStatement, other.thenStatement)
                && Objects.equals(this.elseStatement, other.elseStatement);
    }
}
