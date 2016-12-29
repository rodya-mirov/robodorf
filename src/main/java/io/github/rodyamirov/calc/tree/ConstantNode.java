package io.github.rodyamirov.calc.tree;

import io.github.rodyamirov.calc.visitor.TreeVisitor;

import java.util.Objects;

/**
 * Created by richard.rast on 12/22/16.
 */
public final class ConstantNode extends SyntaxTree<Integer> {
    public final int value;

    public ConstantNode(int value) {
        this.value = value;
    }

    @Override
    public void acceptVisitor(TreeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ConstantNode)) {
            return false;
        }

        ConstantNode other = (ConstantNode)o;

        return Objects.equals(this.value, other.value);
    }
}
