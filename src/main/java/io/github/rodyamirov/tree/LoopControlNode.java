package io.github.rodyamirov.tree;

import io.github.rodyamirov.symbols.Scope;

import java.util.Objects;

/**
 * A holster for loop control nodes -- continue and break.
 *
 * Created by richard.rast on 12/30/16.
 */
public class LoopControlNode extends StatementNode {
    public enum Type { CONTINUE, BREAK }

    public final Type type;

    private LoopControlNode(Scope scope, Type type) {
        super(scope);
        this.type = type;
    }

    public static LoopControlNode Continue(Scope scope) {
        return new LoopControlNode(scope, Type.CONTINUE);
    }

    public static LoopControlNode Break(Scope scope) {
        return new LoopControlNode(scope, Type.BREAK);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof LoopControlNode)) {
            return false;
        }

        LoopControlNode other = (LoopControlNode)o;
        return Objects.equals(this.scope, other.scope)
                && Objects.equals(this.type, other.type);
    }

    @Override
    public int hashCode() {
        return 43 * scope.hashCode() + type.hashCode();
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
