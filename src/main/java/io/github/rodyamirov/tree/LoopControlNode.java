package io.github.rodyamirov.tree;

import io.github.rodyamirov.symbols.Scope;

import java.util.Objects;

/**
 * A holster for loop control nodes -- continue and break.
 *
 * Created by richard.rast on 12/30/16.
 */
public final class LoopControlNode extends StatementNode {
    public enum Type { CONTINUE, BREAK }

    public final Type type;

    private LoopControlNode(Type type) {
        this.type = type;
    }

    public static LoopControlNode Continue() {
        return new LoopControlNode(Type.CONTINUE);
    }

    public static LoopControlNode Break() {
        return new LoopControlNode(Type.BREAK);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof LoopControlNode)) {
            return false;
        }

        LoopControlNode other = (LoopControlNode)o;
        return Objects.equals(this.type, other.type)
                && Objects.equals(this.scope, other.scope);
    }

    @Override
    public int hashCode() {
        return 43 * Objects.hashCode(scope) + type.hashCode();
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
