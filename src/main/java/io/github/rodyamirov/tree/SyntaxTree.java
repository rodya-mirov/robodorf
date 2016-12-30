package io.github.rodyamirov.tree;

/**
 * Created by richard.rast on 12/22/16.
 */
public abstract class SyntaxTree {
    public abstract void acceptVisit(NodeVisitor nodeVisitor);
}
