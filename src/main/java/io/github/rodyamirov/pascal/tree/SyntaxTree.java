package io.github.rodyamirov.pascal.tree;

import io.github.rodyamirov.pascal.visitor.NodeVisitor;

/**
 * Created by richard.rast on 12/22/16.
 */
public abstract class SyntaxTree {
    public abstract void acceptVisit(NodeVisitor nodeVisitor);
}
