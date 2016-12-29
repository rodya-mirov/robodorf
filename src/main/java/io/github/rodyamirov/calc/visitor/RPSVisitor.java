package io.github.rodyamirov.calc.visitor;

import io.github.rodyamirov.calc.tree.BinOpNode;
import io.github.rodyamirov.calc.tree.ConstantNode;
import io.github.rodyamirov.calc.tree.UnaryOpNode;

import java.util.Stack;

/**
 * Created by richard.rast on 12/24/16.
 */
public class RPSVisitor extends TreeVisitor {
    private final Stack<String> stringStack = new Stack<>();

    public String getRPSNotation() {
        return stringStack.peek();
    }

    @Override
    public void visit(ConstantNode constantNode) {
        stringStack.add(String.valueOf(constantNode.value));
    }

    @Override
    public void visit(UnaryOpNode unaryOpNode) {
        String child = stringStack.pop();

        String value = String.format(
                "%s %s",
                child,
                unaryOpNode.opToken.type
        );

        stringStack.push(value);
    }

    @Override
    public void visit(BinOpNode binOpNode) {
        String right = stringStack.pop();
        String left = stringStack.pop();

        String value = String.format(
                "%s %s %s",
                left,
                right,
                binOpNode.opToken.type
        );

        stringStack.push(value);
    }
}
