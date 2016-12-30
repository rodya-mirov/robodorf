package io.github.rodyamirov.tree;

/**
 * Abstract class for visiting SyntaxTree objects. This just includes a lot of "visit" methods.
 * The important thing to remember is that the correct entry point is through the SyntaxTree
 * object's `acceptVisit(NodeVisitor)` method. Those methods do just this:
 * 1.   Call `visit(this)` on the NodeVisitor
 *
 * This is to avoid annoying switch statements inside the "visit" methods. The SyntaxTree classes
 * are all immutable, so they're safe from side effects of the NodeVisitor classes.
 *
 * Implementors of NodeVisitor can do whatever they want with the `visit` methods, but this is how
 * the interface is designed. For example, to evaluate an ExpressionNode (en) the ideal way would
 * be to do this:
 *      EvalVisitor visitor = new EvalVisitor();
 *      en.acceptVisit(visitor)                     // wrapper for visitor.visit(en)
 *      return visitor.resultStack.pop()            // visitor is now cleared out, probably
 *
 * Created by richard.rast on 12/24/16.
 */
public abstract class NodeVisitor {
    public abstract void visit(AndThenNode andThenNode);
    public abstract void visit(AssignNode assignNode);
    public abstract void visit(BinOpNode binOpNode);
    public abstract void visit(BlockNode blockNode);
    public abstract void visit(BooleanConstantNode booleanConstantNode);
    public abstract void visit(CompoundNode compoundNode);
    public abstract void visit(DeclarationNode declarationNode);
    public abstract void visit(IfStatementNode ifStatementNode);
    public abstract void visit(IntConstantNode intConstantNode);
    public abstract void visit(NoOpNode noOpNode);
    public abstract void visit(OrElseNode orElseNode);
    public abstract void visit(ProcedureDeclarationNode procedureDeclarationNode);
    public abstract void visit(ProgramNode programNode);
    public abstract void visit(RealConstantNode intConstantNode);
    public abstract void visit(UnaryOpNode unaryOpNode);
    public abstract void visit(VariableAssignNode variableAssignNode);
    public abstract void visit(VariableDeclarationNode variableDeclarationNode);
    public abstract void visit(VariableEvalNode variableEvalNode);
}
