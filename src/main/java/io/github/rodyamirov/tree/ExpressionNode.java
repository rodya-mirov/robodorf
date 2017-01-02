package io.github.rodyamirov.tree;

import io.github.rodyamirov.symbols.TypeSpec;

/**
 * Created by richard.rast on 12/25/16.
 */
public abstract class ExpressionNode extends SyntaxTree {
    public TypeSpec outputType = null;

    protected ExpressionNode() {
        this.outputType = null;
    }
}
