package io.github.rodyamirov.pascal.tree;

import io.github.rodyamirov.pascal.SymbolValue;
import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.TypeSpec;
import io.github.rodyamirov.pascal.visitor.NodeVisitor;

import java.util.Objects;

/**
 * Created by richard.rast on 12/26/16.
 */
public final class RealConstantNode extends ExpressionNode {
    public final SymbolValue<Float> value;

    public static final TypeSpec DESIRED_TYPE = TypeSpec.REAL;

    public RealConstantNode(SymbolValue<Float> value) {
        this.value = value;
    }

    public static RealConstantNode make(Token token) {
        SymbolValue<Float> sv = SymbolValue.make(DESIRED_TYPE, token.value);
        return new RealConstantNode(sv);
    }

    public static RealConstantNode make(float value) {
        return make(Token.REAL_CONSTANT(value));
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof RealConstantNode)) {
            return false;
        }

        RealConstantNode other = (RealConstantNode)o;

        return Objects.equals(this.value, other.value);
    }
}
