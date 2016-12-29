package io.github.rodyamirov.pascal.tree;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.rodyamirov.pascal.SymbolValue;
import io.github.rodyamirov.pascal.Token;
import io.github.rodyamirov.pascal.visitor.NodeVisitor;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Created by richard.rast on 12/24/16.
 */
public final class BinOpNode extends ExpressionNode {
    public final ExpressionNode left, right;
    public final Token opToken;
    public final BiFunction<SymbolValue, SymbolValue, SymbolValue> function;

    // static it up
    private static final ImmutableMap<Token.Type, BiFunction<SymbolValue, SymbolValue, SymbolValue>> evaluations =
            ImmutableMap.<Token.Type, BiFunction<SymbolValue, SymbolValue, SymbolValue>>builder() // java 7 fail :(
                    .put(Token.Type.PLUS, SymbolValue::add)
                    .put(Token.Type.MINUS, SymbolValue::subtract)
                    .put(Token.Type.TIMES, SymbolValue::multiply)
                    .put(Token.Type.INT_DIVIDE, SymbolValue::intDivide)
                    .put(Token.Type.REAL_DIVIDE, SymbolValue::realDivide)
                    .put(Token.Type.MOD, SymbolValue::intMod)

                    .put(Token.Type.AND, SymbolValue::and)
                    .put(Token.Type.OR, SymbolValue::or)

                    .put(Token.Type.LESS_THAN, SymbolValue::lessThan)
                    .put(Token.Type.LESS_THAN_OR_EQUALS, SymbolValue::lessThanOrEquals)
                    .put(Token.Type.GREATER_THAN, SymbolValue::greaterThan)
                    .put(Token.Type.GREATER_THAN_OR_EQUALS, SymbolValue::greaterThanOrEquals)
                    .put(Token.Type.EQUALS, SymbolValue::equalsValue)
                    .put(Token.Type.NOT_EQUALS, SymbolValue::notEqualsValue)
                    .build();
    private static final ImmutableSet<Token.Type> allowedOpTypes = evaluations.keySet();

    public BinOpNode(ExpressionNode left, ExpressionNode right, Token opToken) {
        if (left == null || right == null || opToken == null) {
            String message = "Both children must be non-null";
            throw new IllegalArgumentException(message);
        }

        if (!allowedOpTypes.contains(opToken.type)) {
            String message = String.format(
                    "Unexpected opToken type %s; expected something from %s",
                    opToken.type, allowedOpTypes);
            throw new IllegalArgumentException(message);
        }

        this.left = left;
        this.right = right;
        this.opToken = opToken;
        this.function = evaluations.get(opToken.type);
    }

    @Override
    public void acceptVisit(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof BinOpNode)) {
            return false;
        }

        BinOpNode other = (BinOpNode)o;

        return Objects.equals(this.opToken, other.opToken)
                && Objects.equals(this.left, other.left)
                && Objects.equals(this.right, other.right);
    }
}
