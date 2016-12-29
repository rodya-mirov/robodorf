package io.github.rodyamirov.calc.visitor;

import io.github.rodyamirov.calc.tree.BinOpNode;
import io.github.rodyamirov.calc.tree.ConstantNode;
import io.github.rodyamirov.calc.tree.UnaryOpNode;

/**
 * Created by richard.rast on 12/23/16.
 */
public abstract class TreeVisitor {
    public abstract void visit(BinOpNode binOpNode);
    public abstract void visit(ConstantNode constantNode);
    public abstract void visit(UnaryOpNode unaryOpNode);
}
