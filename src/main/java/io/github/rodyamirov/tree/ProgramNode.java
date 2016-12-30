package io.github.rodyamirov.tree;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.Scope;

import java.util.Objects;

/**
 * Created by richard.rast on 12/26/16.
 */
public final class ProgramNode extends SyntaxTree {
    public final Token<String> name;
    public final BlockNode blockNode;

    public ProgramNode(Scope scope, Token<String> name, BlockNode blockNode) {
        super(scope);
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
                && Objects.equals(this.blockNode, other.blockNode)
                && Objects.equals(this.scope, other.scope);
    }
}
