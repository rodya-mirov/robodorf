package io.github.rodyamirov.pascal.tree;

import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.visitor.NodeVisitor;

import java.util.Objects;

/**
 * Created by richard.rast on 12/22/16.
 */
public final class VariableEvalNode extends ExpressionNode {
    public final Token idToken;
    public final String id;

    public VariableEvalNode(Token idToken) {
        if (idToken == null || idToken.type != Token.Type.ID) {
            throw new IllegalArgumentException("idToken must be nonnull and of type ID");
        }

        this.idToken = idToken;
        this.id = (String) idToken.value;
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof VariableEvalNode)) {
            return false;
        }

        VariableEvalNode other = (VariableEvalNode)o;

        return Objects.equals(this.idToken, other.idToken)
                && Objects.equals(this.id, other.id);
    }
}
