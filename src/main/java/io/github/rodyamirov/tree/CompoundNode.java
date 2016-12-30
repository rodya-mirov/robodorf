package io.github.rodyamirov.tree;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * Created by richard.rast on 12/22/16.
 */
public final class CompoundNode extends StatementNode {
    public final ImmutableList<StatementNode> statements;

    public CompoundNode(List<StatementNode> statements) {
        this.statements = ImmutableList.copyOf(statements);
    }

    @Override
    public void acceptVisit(NodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof CompoundNode)) {
            return false;
        }

        CompoundNode other = (CompoundNode)o;

        return Objects.equals(this.statements, other.statements);
    }
}
