package io.github.rodyamirov.analysis;

import com.google.common.collect.ImmutableSet;
import io.github.rodyamirov.exceptions.TypeCheckException;
import io.github.rodyamirov.symbols.SymbolTable;
import io.github.rodyamirov.symbols.TypeSpec;
import io.github.rodyamirov.tree.AndThenNode;
import io.github.rodyamirov.tree.AssignNode;
import io.github.rodyamirov.tree.BinOpNode;
import io.github.rodyamirov.tree.BooleanConstantNode;
import io.github.rodyamirov.tree.ExpressionNode;
import io.github.rodyamirov.tree.IntConstantNode;
import io.github.rodyamirov.tree.OrElseNode;
import io.github.rodyamirov.tree.RealConstantNode;
import io.github.rodyamirov.tree.SyntaxTree;
import io.github.rodyamirov.tree.ThoroughVisitor;
import io.github.rodyamirov.tree.UnaryOpNode;
import io.github.rodyamirov.tree.VariableAssignNode;
import io.github.rodyamirov.tree.VariableEvalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This does a global sweep of a parse tree to assign types to expressions and check the validity /
 * appropriateness of those types. It checks that expression composition is well-defined (e.g.
 * raising an error message if you try to multiply two booleans) and checks that variable assignment
 * is well-defined (e.g. not trying to assign a boolean value to an integer variable).
 *
 * Created by richard.rast on 1/2/17.
 */
public class TypeChecker extends ThoroughVisitor {
    // TODO - lots of tests

    public static List<ErrorMessage> assignTypes(SyntaxTree syntaxTree, SymbolTable symbolTable) {
        TypeChecker typeChecker = new TypeChecker(symbolTable);
        syntaxTree.acceptVisit(typeChecker);
        return typeChecker.errorMessages;
    }

    private List<ErrorMessage> errorMessages = new ArrayList<>();
    private final SymbolTable symbolTable;

    private static final Set<TypeSpec> INTEGER_OR_REAL = ImmutableSet.of(TypeSpec.INTEGER, TypeSpec.REAL);
    private static final Set<TypeSpec> INTEGER = ImmutableSet.of(TypeSpec.INTEGER);
    private static final Set<TypeSpec> BOOLEAN = ImmutableSet.of(TypeSpec.BOOLEAN);

    private TypeChecker(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    private boolean checkTypes(Set<TypeSpec> allowed, ExpressionNode toCheck) {
        if (! allowed.contains(toCheck.outputType)) {
            errorMessages.add(new ErrorMessage(String.format("Expected type among %s, got %s", allowed, toCheck.outputType), toCheck));
            return false;
        } else {
            return true;
        }
    }

    private TypeSpec arithmeticIntToFloat(TypeSpec left, TypeSpec right) {
        switch (left) {
            case REAL:
                switch (right) {
                    case INTEGER:
                    case REAL:
                        return TypeSpec.REAL;

                    default:
                        throw TypeCheckException.conversionImpossible(right, TypeSpec.REAL);
                }

            case INTEGER:
                switch (right) {
                    case INTEGER: return TypeSpec.INTEGER;
                    case REAL: return TypeSpec.REAL;

                    default:
                        throw TypeCheckException.conversionImpossible(right, TypeSpec.REAL);
                }

            default:
                throw TypeCheckException.conversionImpossible(left, TypeSpec.REAL);
        }
    }

    @Override
    public void visit(AssignNode assignNode) {
        super.visit(assignNode);

        TypeSpec actualType = assignNode.expressionNode.outputType;

        // if we got an error lower down there's no point ...
        if (actualType != null) {
            VariableAssignNode van = assignNode.variableAssignNode;
            TypeSpec goalType = symbolTable.getType(van.scope, van.idToken);

            // no output for this, we're just checking validity
            switch (goalType) {
                case INTEGER:
                    checkTypes(INTEGER, assignNode.expressionNode);
                    break;

                case REAL:
                    checkTypes(INTEGER_OR_REAL, assignNode.expressionNode);
                    break;

                case BOOLEAN:
                    checkTypes(BOOLEAN, assignNode.expressionNode);
                    break;

                default:
                    String message = String.format(
                            "Unrecognized variable type %s",
                            goalType.name()
                    );
                    throw new IllegalArgumentException(message);
            }
        }
    }

    @Override
    public void visit(BinOpNode binOpNode) {
        super.visit(binOpNode);

        ExpressionNode leftInput = binOpNode.left;
        ExpressionNode rightInput = binOpNode.right;

        // if we got an error lower down it doesn't make sense to evaluate the output here
        if (leftInput.outputType == null || rightInput.outputType == null) {
            return;
        }

        TypeSpec output;
        switch (binOpNode.opToken.type) {
            case MINUS:
            case PLUS:
            case TIMES:
                if (!checkTypes(INTEGER_OR_REAL, leftInput)) return;
                if (!checkTypes(INTEGER_OR_REAL, rightInput)) return;
                output = arithmeticIntToFloat(leftInput.outputType, rightInput.outputType);
                break;

            case REAL_DIVIDE:
                if (!checkTypes(INTEGER_OR_REAL, leftInput)) return;
                if (!checkTypes(INTEGER_OR_REAL, rightInput)) return;
                output = TypeSpec.REAL;
                break;

            case INT_DIVIDE:
            case MOD:
                if (!checkTypes(INTEGER, leftInput)) return;
                if (!checkTypes(INTEGER, rightInput)) return;
                output = TypeSpec.INTEGER;
                break;

            case AND:
            case OR:
                if (!checkTypes(BOOLEAN, leftInput)) return;
                if (!checkTypes(BOOLEAN, rightInput)) return;
                output = TypeSpec.BOOLEAN;
                break;

            case LESS_THAN:
            case LESS_THAN_OR_EQUALS:
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUALS:
                if (!checkTypes(INTEGER_OR_REAL, leftInput)) return;
                if (!checkTypes(INTEGER_OR_REAL, rightInput)) return;
                output = TypeSpec.BOOLEAN;
                break;

            case EQUALS:
            case NOT_EQUALS:
                if (! checkTypes(ImmutableSet.of(leftInput.outputType), rightInput)) return;
                output = TypeSpec.BOOLEAN;
                break;

            default:
                String message = String.format(
                        "Unrecognized binary operation type %s",
                        binOpNode.opToken.type.name()
                );
                throw new IllegalArgumentException(message);
        }

        binOpNode.outputType = output;
    }

    @Override
    public void visit(BooleanConstantNode booleanConstantNode) {
        super.visit(booleanConstantNode);
        booleanConstantNode.outputType = TypeSpec.BOOLEAN;
    }

    @Override
    public void visit(IntConstantNode intConstantNode) {
        super.visit(intConstantNode);
        intConstantNode.outputType = TypeSpec.INTEGER;
    }

    @Override
    public void visit(AndThenNode andThenNode) {
        super.visit(andThenNode);

        boolean foundError = false;

        if (andThenNode.left.outputType == null || !checkTypes(BOOLEAN, andThenNode.left)) {
            foundError = true;
        }

        if (andThenNode.right.outputType == null || !checkTypes(BOOLEAN, andThenNode.right)) {
            foundError = true;
        }

        if (!foundError) {
            andThenNode.outputType = TypeSpec.BOOLEAN;
        }
    }

    @Override
    public void visit(OrElseNode orElseNode) {
        super.visit(orElseNode);

        boolean foundError = false;

        if (orElseNode.left.outputType == null || !checkTypes(BOOLEAN, orElseNode.left)) {
            foundError = true;
        }

        if (orElseNode.right.outputType == null || !checkTypes(BOOLEAN, orElseNode.right)) {
            foundError = true;
        }

        if (!foundError) {
            orElseNode.outputType = TypeSpec.BOOLEAN;
        }
    }

    @Override
    public void visit(RealConstantNode realConstantNode) {
        super.visit(realConstantNode);
        realConstantNode.outputType = TypeSpec.REAL;
    }

    @Override
    public void visit(UnaryOpNode unaryOpNode) {
        super.visit(unaryOpNode);

        TypeSpec inputType = unaryOpNode.child.outputType;
        if (inputType == null) {
            return;
        }

        TypeSpec outputType;

        switch (unaryOpNode.opToken.type) {
            case PLUS:
            case MINUS:
                if (!checkTypes(INTEGER_OR_REAL, unaryOpNode.child)) return;
                outputType = unaryOpNode.child.outputType;
                break;

            case NOT:
                if (!checkTypes(BOOLEAN, unaryOpNode.child)) return;
                outputType = TypeSpec.BOOLEAN;
                break;

            default:
                String message = String.format(
                        "Unrecognized unary operation type %s",
                        unaryOpNode.opToken.type.name()
                );
                throw new IllegalArgumentException(message);
        }
    }

    @Override
    public void visit(VariableEvalNode variableEvalNode) {
        super.visit(variableEvalNode);

        variableEvalNode.outputType =
                symbolTable.getType(variableEvalNode.scope, variableEvalNode.idToken);
    }
}
