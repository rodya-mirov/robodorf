package io.github.rodyamirov.pascal.tree;

import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.Tokenizer;
import io.github.rodyamirov.pascal.visitor.NodeVisitor;

import java.util.Objects;

/**
 * Created by richard.rast on 12/26/16.
 */
public final class ProgramNode extends SyntaxTree {
    public final Token<String> name;
    public final BlockNode blockNode;

    public ProgramNode(Token<String> name, BlockNode blockNode) {
        this.name = name;
        this.blockNode = blockNode;
    }

    @Override
    public void acceptVisit(NodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ProgramNode)) {
            return false;
        }

        ProgramNode other = (ProgramNode)o;

        return Objects.equals(this.name, other.name)
                && Objects.equals(this.blockNode, other.blockNode);
    }
}
