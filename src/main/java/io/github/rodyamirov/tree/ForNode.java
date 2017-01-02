package io.github.rodyamirov.tree;

import java.util.Objects;

/**
 * Created by richard.rast on 1/1/17.
 */
public class ForNode extends LoopStatementNode {
    public enum Direction { FORWARD, BACKWARD }

    public final AssignNode assignNode;
    public final ExpressionNode bound;
    public final StatementNode body;
    public final Direction direction;

    private ForNode(AssignNode assignNode, ExpressionNode bound, StatementNode body, Direction direction) {
        this.assignNode = assignNode;
        this.bound = bound;
        this.body = body;
        this.direction = direction;
    }

    public static ForNode Forward(AssignNode assignNode, ExpressionNode bound, StatementNode body) {
        return new ForNode(assignNode, bound, body, Direction.FORWARD);
    }

    public static ForNode Backward(AssignNode assignNode, ExpressionNode bound, StatementNode body) {
        return new ForNode(assignNode, bound, body, Direction.BACKWARD);
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ForNode)) {
            return false;
        }

        ForNode other = (ForNode)o;

        return Objects.equals(this.assignNode, other.assignNode)
                && Objects.equals(this.bound, other.bound)
                && Objects.equals(this.body, other.body)
                && Objects.equals(this.direction, other.direction)
                && Objects.equals(this.scope, other.scope);
    }

    @Override
    public int hashCode() {
        int hashCode = Objects.hashCode(scope);
        hashCode = 43 * hashCode + assignNode.hashCode();
        hashCode = 43 * hashCode + bound.hashCode();
        hashCode = 43 * hashCode + body.hashCode();
        return 43 * hashCode + direction.hashCode();
    }
}
