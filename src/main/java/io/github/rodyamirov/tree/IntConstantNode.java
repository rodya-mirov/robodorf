package io.github.rodyamirov.tree;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.SymbolValue;
import io.github.rodyamirov.symbols.TypeSpec;

import java.util.Objects;

/**
 * Created by richard.rast on 12/24/16.
 */
public final class IntConstantNode extends TerminalExpressionNode {
    public final SymbolValue<Integer> value;

    public static final TypeSpec DESIRED_TYPE = TypeSpec.INTEGER;

    private IntConstantNode(SymbolValue<Integer> value) {
        this.value = value;
    }

    public static IntConstantNode make(Token token) {
        SymbolValue<Integer> sv = SymbolValue.make(DESIRED_TYPE, token.value);
        return new IntConstantNode(sv);
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

        return Objects.equals(this.value, other.value)
                && Objects.equals(this.scope, other.scope);
    }

    @Override
    public int hashCode() {
        int out = Objects.hashCode(scope);
        out = 43 * out + value.hashCode();
        return out;
    }
}
