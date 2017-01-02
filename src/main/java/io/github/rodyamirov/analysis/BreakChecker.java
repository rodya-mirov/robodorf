package io.github.rodyamirov.analysis;

import io.github.rodyamirov.tree.DoUntilNode;
import io.github.rodyamirov.tree.ForNode;
import io.github.rodyamirov.tree.LoopControlNode;
import io.github.rodyamirov.tree.LoopStatementNode;
import io.github.rodyamirov.tree.ProcedureCallNode;
import io.github.rodyamirov.tree.SyntaxTree;
import io.github.rodyamirov.tree.ThoroughVisitor;
import io.github.rodyamirov.tree.WhileNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by richard.rast on 1/2/17.
 */
public class BreakChecker extends ThoroughVisitor {
    private Stack<LoopStatementNode> loopStack;
    private List<ErrorMessage> errorMessages;

    public static List<ErrorMessage> check(SyntaxTree syntaxTree) {
        BreakChecker breakChecker = new BreakChecker();
        syntaxTree.acceptVisit(breakChecker);
        return breakChecker.errorMessages;
    }

    public BreakChecker() {
        loopStack = new Stack<>();
        errorMessages = new ArrayList<>();
    }

    @Override
    public void visit(WhileNode whileNode) {
        loopStack.push(whileNode);
        super.visit(whileNode);
        loopStack.pop();
    }

    @Override
    public void visit(DoUntilNode doUntilNode) {
        loopStack.push(doUntilNode);
        super.visit(doUntilNode);
        loopStack.pop();
    }

    @Override
    public void visit(ForNode forNode) {
        loopStack.push(forNode);
        super.visit(forNode);
        loopStack.pop();
    }

    @Override
    public void visit(ProcedureCallNode procedureCallNode) {
        Stack<LoopStatementNode> outsideLoopStack = loopStack;
        loopStack = new Stack<>();
        super.visit(procedureCallNode);
        loopStack = outsideLoopStack;
    }

    @Override
    public void visit(LoopControlNode loopControlNode) {
        if (loopStack.isEmpty()) {
            ErrorMessage message = new ErrorMessage(
                    String.format(
                            "Encountered loop control node %s outside of loop.",
                            loopControlNode.toString()
                    ),
                    loopControlNode
            );
            errorMessages.add(message);
        }
        super.visit(loopControlNode);
    }
}
