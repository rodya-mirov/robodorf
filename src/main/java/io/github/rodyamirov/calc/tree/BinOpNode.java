package io.github.rodyamirov.calc.tree;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.rodyamirov.calc.Token;
import io.github.rodyamirov.calc.visitor.TreeVisitor;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Created by richard.rast on 12/22/16.
 */
public final class BinOpNode extends SyntaxTree<Integer> {
    public final SyntaxTree<Integer> left, right;
    public final Token opToken;
    public final BiFunction<Integer, Integer, Integer> function;

    // static it up
    private static final ImmutableMap<Token.Type, BiFunction<Integer, Integer, Integer>> evaluations =
            ImmutableMap.of(
                    Token.Type.PLUS, (x, y) -> x+y,
                    Token.Type.MINUS, (x, y) -> x-y,
                    Token.Type.TIMES, (x, y) -> x*y,
                    Token.Type.DIVIDE, (x, y) -> x/y
            );
    private static final ImmutableSet<Token.Type> allowedOpTypes = evaluations.keySet();

    public BinOpNode(SyntaxTree<Integer> left, SyntaxTree<Integer> right, Token opToken) {
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
    public void acceptVisitor(TreeVisitor visitor) {
        left.acceptVisitor(visitor);
        right.acceptVisitor(visitor);

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
