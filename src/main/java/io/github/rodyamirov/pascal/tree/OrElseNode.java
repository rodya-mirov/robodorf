package io.github.rodyamirov.pascal.tree;

import io.github.rodyamirov.pascal.visitor.NodeVisitor;

import java.util.Objects;

/**
 * Created by richard.rast on 12/29/16.
 */
public final class OrElseNode extends ExpressionNode {
    public final ExpressionNode left, right;

    public OrElseNode(ExpressionNode left, ExpressionNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof OrElseNode)) {
            return false;
        }

        OrElseNode other = (OrElseNode) o;
        return Objects.equals(this.left, other.left)
                && Objects.equals(this.right, other.right);
    }
}
