package io.github.rodyamirov.pascal.tree;

import io.github.rodyamirov.pascal.SymbolValue;
import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.TypeSpec;
import io.github.rodyamirov.pascal.visitor.NodeVisitor;

import java.util.Objects;

/**
 * Created by richard.rast on 12/29/16.
 */
public final class BooleanConstantNode extends TerminalExpressionNode {
    public final SymbolValue<Boolean> value;

    public static final TypeSpec DESIRED_TYPE = TypeSpec.BOOLEAN;

    public BooleanConstantNode(SymbolValue<Boolean> value) {
        this.value = value;
    }

    public static BooleanConstantNode make(Token token) {
        SymbolValue<Boolean> sv = SymbolValue.make(DESIRED_TYPE, token.value);
        return new BooleanConstantNode(sv);
    }

    public static BooleanConstantNode make(boolean value) {
        return make(Token.BOOLEAN_CONSTANT(value));
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof BooleanConstantNode)) {
            return false;
        }

        BooleanConstantNode other = (BooleanConstantNode) o;

        return Objects.equals(this.value, other.value);
    }
}
