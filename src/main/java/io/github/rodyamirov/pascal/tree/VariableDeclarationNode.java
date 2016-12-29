package io.github.rodyamirov.pascal.tree;

import com.google.common.collect.ImmutableList;
import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.TypeSpec;
import io.github.rodyamirov.pascal.visitor.NodeVisitor;

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
                && Objects.equals(this.varType, other.varType);
    }
}
