package io.github.rodyamirov.tree;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.Scope;

import java.util.Objects;

/**
 * Created by richard.rast on 12/30/16.
 */
public final class ProcedureCallNode extends StatementNode {
    public final Token<String> procedureName;

    public ProcedureCallNode(Scope scope, Token<String> procedureName) {
        super(scope);
        this.procedureName = procedureName;
    }

    @Override
    public void acceptVisit(NodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ProcedureCallNode)) {
            return false;
        }

        ProcedureCallNode other = (ProcedureCallNode)o;
        return Objects.equals(this.procedureName, other.procedureName)
                && Objects.equals(this.scope, other.scope);
    }

    @Override
    public int hashCode() {
        return scope.hashCode() * 43 + procedureName.hashCode();
    }
}
