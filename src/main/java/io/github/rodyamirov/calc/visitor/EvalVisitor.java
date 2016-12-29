package io.github.rodyamirov.calc.visitor;

import io.github.rodyamirov.calc.tree.BinOpNode;
import io.github.rodyamirov.calc.tree.ConstantNode;
import io.github.rodyamirov.calc.tree.UnaryOpNode;
import io.github.rodyamirov.pascal.tree.SyntaxTree;

import java.util.Stack;

/**
 * Created by richard.rast on 12/23/16.
 */
public class EvalVisitor extends TreeVisitor {
    private final Stack<Integer> integerStack = new Stack<>();

    public EvalVisitor() {
        // do nothing
    }

    public int getValue() {
        return integerStack.peek();
    }

    @Override
    public void visit(ConstantNode constantNode) {
        integerStack.push(constantNode.value);
    }

    @Override
    public void visit(BinOpNode binOpNode) {
        int right = integerStack.pop();
        int left = integerStack.pop();

        int value = binOpNode.function.apply(left, right);
        integerStack.push(value);
    }

    @Override
    public void visit(UnaryOpNode unaryOpNode) {
        int child = integerStack.pop();

        int value = unaryOpNode.function.apply(child);
        integerStack.push(value);
    }
}
