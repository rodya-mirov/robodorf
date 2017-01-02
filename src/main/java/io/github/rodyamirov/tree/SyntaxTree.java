package io.github.rodyamirov.tree;

import io.github.rodyamirov.symbols.Scope;

/**
 * Created by richard.rast on 12/22/16.
 */
public abstract class SyntaxTree {
    public Scope scope;

    protected SyntaxTree() {
        scope = null;
    }

    public abstract void acceptVisit(NodeVisitor nodeVisitor);
}
