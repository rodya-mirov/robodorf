package io.github.rodyamirov.tree;

import io.github.rodyamirov.lex.Token;

import java.util.Objects;

/**
 * Created by richard.rast on 12/28/16.
 */
public final class ProcedureDeclarationNode extends SyntaxTree {
    public final Token<String> name;
    public final BlockNode blockNode;

    public ProcedureDeclarationNode(Token<String> name, BlockNode blockNode) {
        this.name = name;
        this.blockNode = blockNode;
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ProcedureDeclarationNode)) {
            return false;
        }

        ProcedureDeclarationNode other = (ProcedureDeclarationNode)o;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.blockNode, other.blockNode);
    }
}
