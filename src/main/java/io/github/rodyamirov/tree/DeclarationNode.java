package io.github.rodyamirov.tree;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * Created by richard.rast on 12/28/16.
 */
public final class DeclarationNode extends SyntaxTree {
    public final ImmutableList<VariableDeclarationNode> variableDeclarations;
    public final ImmutableList<ProcedureDeclarationNode> procedureDeclarations;

    public DeclarationNode(
            List<VariableDeclarationNode> variableDeclarations,
            List<ProcedureDeclarationNode> procedureDeclarations) {
        this.variableDeclarations = ImmutableList.copyOf(variableDeclarations);
        this.procedureDeclarations = ImmutableList.copyOf(procedureDeclarations);
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DeclarationNode)) {
            return false;
        }

        DeclarationNode other = (DeclarationNode)o;

        return Objects.equals(this.variableDeclarations, other.variableDeclarations)
                && Objects.equals(this.procedureDeclarations, other.procedureDeclarations)
                && Objects.equals(this.scope, other.scope);
    }

    @Override
    public int hashCode() {
        int out = Objects.hashCode(scope);
        out = 43 * out + variableDeclarations.hashCode();
        out = 43 * out + procedureDeclarations.hashCode();
        return out;
    }
}
