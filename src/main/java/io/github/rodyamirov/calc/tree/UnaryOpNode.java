package io.github.rodyamirov.calc.tree;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.rodyamirov.calc.Token;
import io.github.rodyamirov.calc.visitor.TreeVisitor;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by richard.rast on 12/22/16.
 */
public final class UnaryOpNode extends SyntaxTree<Integer> {
    public final SyntaxTree<Integer> child;
    public final Token opToken;
    public final Function<Integer, Integer> function;

    // static it up
    private static final ImmutableMap<Token.Type, Function<Integer, Integer>> evaluations =
            ImmutableMap.of(
                    Token.Type.PLUS, x -> x,
                    Token.Type.MINUS, x -> -x
            );
    private static final ImmutableSet<Token.Type> allowedOpTypes = evaluations.keySet();


    public UnaryOpNode(SyntaxTree<Integer> child, Token opToken) {
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
    public void acceptVisitor(TreeVisitor visitor) {
        child.acceptVisitor(visitor);

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
