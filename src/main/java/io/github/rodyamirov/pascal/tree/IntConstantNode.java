package io.github.rodyamirov.pascal.tree;

import io.github.rodyamirov.pascal.SymbolValue;
import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.TypeSpec;
import io.github.rodyamirov.pascal.visitor.NodeVisitor;
import io.github.rodyamirov.pascal.visitor.TypeCheckException;
import io.github.rodyamirov.pascal.visitor.VariableException;

import java.util.Objects;

/**
 * Created by richard.rast on 12/24/16.
 */
public final class IntConstantNode extends ExpressionNode {
    public final SymbolValue<Integer> value;

    public static final TypeSpec DESIRED_TYPE = TypeSpec.INT;

    public IntConstantNode(SymbolValue<Integer> value) {
        if (value.typeSpec != DESIRED_TYPE) {
            throw TypeCheckException.wrongValueClass(value.typeSpec, DESIRED_TYPE);
        }

        this.value = value;
    }

    public static IntConstantNode make(Token token) {
        SymbolValue<Integer> sv = SymbolValue.make(DESIRED_TYPE, token.value);
        return new IntConstantNode(sv);
    }

    public static IntConstantNode make(int value) {
        return make(Token.INT_CONSTANT(value));
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof IntConstantNode)) {
            return false;
        }

        IntConstantNode other = (IntConstantNode)o;

        return Objects.equals(this.value, other.value);
    }
}
