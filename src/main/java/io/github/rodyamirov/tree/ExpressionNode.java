package io.github.rodyamirov.tree;

import io.github.rodyamirov.symbols.Scope;

/**
 * Created by richard.rast on 12/25/16.
 */
public abstract class ExpressionNode extends SyntaxTree {
    protected ExpressionNode(Scope scope) {
        super(scope);
    }
}
