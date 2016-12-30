package io.github.rodyamirov.tree;

import io.github.rodyamirov.symbols.Scope;

/**
 * Created by richard.rast on 12/22/16.
 */
public abstract class SyntaxTree {
    public final Scope scope;

    protected SyntaxTree(Scope scope) {
        if (scope == null) {
            throw new NullPointerException("Scope cannot be null!");
        }

        this.scope = scope;
    }

    public abstract void acceptVisit(NodeVisitor nodeVisitor);
}
