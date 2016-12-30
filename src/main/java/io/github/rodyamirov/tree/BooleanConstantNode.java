package io.github.rodyamirov.tree;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.Scope;
import io.github.rodyamirov.symbols.SymbolValue;
import io.github.rodyamirov.symbols.TypeSpec;

import java.util.Objects;

/**
 * Created by richard.rast on 12/29/16.
 */
public final class BooleanConstantNode extends TerminalExpressionNode {
    public final SymbolValue<Boolean> value;

    public static final TypeSpec DESIRED_TYPE = TypeSpec.BOOLEAN;

    private BooleanConstantNode(Scope scope, SymbolValue<Boolean> value) {
        super(scope);
        this.value = value;
    }

    public static BooleanConstantNode make(Scope scope, Token token) {
        SymbolValue<Boolean> sv = SymbolValue.make(DESIRED_TYPE, token.value);
        return new BooleanConstantNode(scope, sv);
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
                && Objects.equals(this.scope, other.scope);
    }
}
