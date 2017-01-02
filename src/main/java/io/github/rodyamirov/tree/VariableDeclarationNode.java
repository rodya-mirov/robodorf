package io.github.rodyamirov.tree;

import com.google.common.collect.ImmutableList;
import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.Scope;
import io.github.rodyamirov.symbols.TypeSpec;

import java.util.List;
import java.util.Objects;

/**
 * Created by richard.rast on 12/26/16.
 */
public final class VariableDeclarationNode extends SyntaxTree {
    public final ImmutableList<Token<String>> variableIds;
    public final TypeSpec varType;

    public VariableDeclarationNode(List<Token<String>> variableIds, TypeSpec varType) {
        this.variableIds = ImmutableList.copyOf(variableIds);
        this.varType = varType;
    }

    @Override
    public void acceptVisit(NodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof VariableDeclarationNode)) {
            return false;
        }

        VariableDeclarationNode other = (VariableDeclarationNode)o;

        return Objects.equals(this.variableIds, other.variableIds)
                && Objects.equals(this.varType, other.varType)
                && Objects.equals(this.scope, other.scope);
    }

    @Override
    public int hashCode() {
        int out = Objects.hashCode(scope);
        out = 43 * out + variableIds.hashCode();
        out = 43 * out + varType.hashCode();
        return out;
    }
}
