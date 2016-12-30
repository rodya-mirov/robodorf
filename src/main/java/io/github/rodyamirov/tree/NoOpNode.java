package io.github.rodyamirov.tree;

/**
 * Created by richard.rast on 12/22/16.
 */
public final class NoOpNode extends StatementNode {
    @Override
    public void acceptVisit(NodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof NoOpNode)) {
            return false;
        }

        return true;
    }
}
