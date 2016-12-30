package io.github.rodyamirov.tree;

import io.github.rodyamirov.symbols.Scope;

import java.util.Objects;

/**
 * Created by richard.rast on 12/22/16.
 */
public final class NoOpNode extends StatementNode {
    public NoOpNode(Scope scope) {
        super(scope);
    }

    @Override
    public void acceptVisit(NodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof NoOpNode)) {
            return false;
        }

        NoOpNode other = (NoOpNode)o;
        return Objects.equals(this.scope, other.scope);
    }
}
