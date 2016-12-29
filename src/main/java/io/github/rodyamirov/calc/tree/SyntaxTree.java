package io.github.rodyamirov.calc.tree;

import io.github.rodyamirov.calc.visitor.EvalVisitor;
import io.github.rodyamirov.calc.visitor.LispVisitor;
import io.github.rodyamirov.calc.visitor.RPSVisitor;
import io.github.rodyamirov.calc.visitor.TreeVisitor;

/**
 * Created by richard.rast on 12/22/16.
 */
public abstract class SyntaxTree<T> {
    public abstract void acceptVisitor(TreeVisitor treeVisitor);

    public final int evaluate() {
        EvalVisitor evalVisitor = new EvalVisitor();
        acceptVisitor(evalVisitor);
        return evalVisitor.getValue();
    }

    public final String lispNotation() {
        LispVisitor lispVisitor = new LispVisitor();
        acceptVisitor(lispVisitor);
        return lispVisitor.getLispNotation();
    }

    public final String reversePolishNotation() {
        RPSVisitor rpsVisitor = new RPSVisitor();
        acceptVisitor(rpsVisitor);
        return rpsVisitor.getRPSNotation();
    }
}
