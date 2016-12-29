package io.github.rodyamirov.pascal.tree;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.rodyamirov.pascal.SymbolValue;
import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.visitor.NodeVisitor;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by richard.rast on 12/24/16.
 */
public final class UnaryOpNode extends ExpressionNode {
    public final ExpressionNode child;
    public final Token opToken;
    public final Function<SymbolValue, SymbolValue> function;

    // static it up
    private static final ImmutableMap<Token.Type, Function<SymbolValue, SymbolValue>> evaluations =
            ImmutableMap.of(
                    Token.Type.PLUS, SymbolValue::pos,
                    Token.Type.MINUS, SymbolValue::neg
            );
    private static final ImmutableSet<Token.Type> allowedOpTypes = evaluations.keySet();


    public UnaryOpNode(ExpressionNode child, Token opToken) {
        if (child == null || opToken == null) {
            throw new IllegalArgumentException("All arguments must be non-null");
        } else if (!allowedOpTypes.contains(opToken.type)) {
            String message = String.format(
                    "Unexpected type %s; expected type among %s",
                    opToken.type, allowedOpTypes);
            throw new IllegalArgumentException(message);
        }

        this.child = child;
        this.opToken = opToken;
        this.function = evaluations.get(opToken.type);
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof UnaryOpNode)) {
            return false;
        }

        UnaryOpNode other = (UnaryOpNode)o;

        return Objects.equals(this.opToken, other.opToken)
                && Objects.equals(this.child, other.child);
    }
}
