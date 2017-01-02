package io.github.rodyamirov.tree;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.SymbolValue;
import io.github.rodyamirov.symbols.TypeSpec;

import java.util.Objects;

/**
 * Created by richard.rast on 12/29/16.
 */
public final class BooleanConstantNode extends TerminalExpressionNode {
    public final SymbolValue<Boolean> value;

    public static final TypeSpec DESIRED_TYPE = TypeSpec.BOOLEAN;

    private BooleanConstantNode(SymbolValue<Boolean> value) {
        this.value = value;
    }

    public static BooleanConstantNode make(Token token) {
        SymbolValue<Boolean> sv = SymbolValue.make(DESIRED_TYPE, token.value);
        return new BooleanConstantNode(sv);
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

        return Objects.equals(this.value, other.value)
                && Objects.equals(this.outputType, other.outputType)
                && Objects.equals(this.scope, other.scope);
    }

    @Override
    public int hashCode() {
        int out = Objects.hashCode(scope);
        out = 43 * out + value.hashCode();
        return out;
    }
}
