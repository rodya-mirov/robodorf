package io.github.rodyamirov.tree;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.Scope;

import java.util.Objects;

/**
 * Created by richard.rast on 12/22/16.
 */
public final class VariableEvalNode extends TerminalExpressionNode {
    public final Token<String> idToken;

    public VariableEvalNode(Scope scope, Token<String> idToken) {
        super(scope);

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
        if (o == null || !(o instanceof VariableEvalNode)) {
            return false;
        }

        VariableEvalNode other = (VariableEvalNode)o;

        return Objects.equals(this.idToken, other.idToken)
                && Objects.equals(this.scope, other.scope);
    }
}
