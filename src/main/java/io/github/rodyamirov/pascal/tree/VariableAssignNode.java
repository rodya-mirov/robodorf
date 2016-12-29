package io.github.rodyamirov.pascal.tree;

import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.visitor.NodeVisitor;

import java.util.Objects;

/**
 * Created by richard.rast on 12/25/16.
 */
public final class VariableAssignNode extends SyntaxTree {
    public final Token<String> idToken;

    public VariableAssignNode(Token<String> idToken) {
        if (idToken == null || idToken.type != Token.Type.ID) {
            throw new IllegalArgumentException("idToken must be nonnull and of type ID");
        }

        this.idToken = idToken;
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof VariableAssignNode)) {
            return false;
        }

        VariableAssignNode other = (VariableAssignNode)o;

        return Objects.equals(this.idToken, other.idToken);
    }
}
