package io.github.rodyamirov.tree;

import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.Scope;
import io.github.rodyamirov.symbols.SymbolValue;
import io.github.rodyamirov.symbols.TypeSpec;

import java.util.Objects;

/**
 * Created by richard.rast on 12/26/16.
 */
public final class RealConstantNode extends TerminalExpressionNode {
    public final SymbolValue<Float> value;

    public static final TypeSpec DESIRED_TYPE = TypeSpec.REAL;

    private RealConstantNode(Scope scope, SymbolValue<Float> value) {
        super(scope);
        this.value = value;
    }

    public static RealConstantNode make(Scope scope, Token token) {
        SymbolValue<Float> sv = SymbolValue.make(DESIRED_TYPE, token.value);
        return new RealConstantNode(scope, sv);
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

        return Objects.equals(this.value, other.value)
                && Objects.equals(this.scope, other.scope);
    }
}
