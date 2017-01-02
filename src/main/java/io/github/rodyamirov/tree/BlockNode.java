package io.github.rodyamirov.tree;

import io.github.rodyamirov.symbols.Scope;

import java.util.Objects;

/**
 * Created by richard.rast on 12/26/16.
 */
public final class BlockNode extends SyntaxTree {
    public final DeclarationNode declarationNode;
    public final CompoundNode compoundNode;

    public BlockNode(DeclarationNode declarationNode, CompoundNode compoundNode) {
        this.declarationNode = declarationNode;
        this.compoundNode = compoundNode;
    }

    @Override
    public void acceptVisit(NodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof BlockNode)) {
            return false;
        }

        BlockNode other = (BlockNode)o;

        return Objects.equals(this.declarationNode, other.declarationNode)
                && Objects.equals(this.compoundNode, other.compoundNode)
                && Objects.equals(this.scope, other.scope);
    }

    @Override
    public int hashCode() {
        int out = Objects.hashCode(scope);
        out = 43 * out + declarationNode.hashCode();
        out = 43 * out + compoundNode.hashCode();
        return out;
    }
}
