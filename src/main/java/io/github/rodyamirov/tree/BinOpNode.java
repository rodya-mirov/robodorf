package io.github.rodyamirov.tree;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.SymbolValue;
import io.github.rodyamirov.symbols.SymbolValueOps;

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
                    .put(Token.Type.PLUS, SymbolValueOps::add)
                    .put(Token.Type.MINUS, SymbolValueOps::subtract)
                    .put(Token.Type.TIMES, SymbolValueOps::multiply)
                    .put(Token.Type.INT_DIVIDE, SymbolValueOps::intDivide)
                    .put(Token.Type.REAL_DIVIDE, SymbolValueOps::realDivide)
                    .put(Token.Type.MOD, SymbolValueOps::intMod)

                    .put(Token.Type.AND, SymbolValueOps::and)
                    .put(Token.Type.OR, SymbolValueOps::or)

                    .put(Token.Type.LESS_THAN, SymbolValueOps::lessThan)
                    .put(Token.Type.LESS_THAN_OR_EQUALS, SymbolValueOps::lessThanOrEquals)
                    .put(Token.Type.GREATER_THAN, SymbolValueOps::greaterThan)
                    .put(Token.Type.GREATER_THAN_OR_EQUALS, SymbolValueOps::greaterThanOrEquals)
                    .put(Token.Type.EQUALS, SymbolValueOps::equalsValue)
                    .put(Token.Type.NOT_EQUALS, SymbolValueOps::notEqualsValue)
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
                && Objects.equals(this.right, other.right)
                && Objects.equals(this.scope, other.scope);
    }

    @Override
    public int hashCode() {
        int out = Objects.hashCode(scope);
        out = 43 * out + opToken.hashCode();
        out = 43 * out + left.hashCode();
        out = 43 * out + right.hashCode();
        return out;
    }
}
