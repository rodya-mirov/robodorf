package io.github.rodyamirov.parse;

import com.google.common.collect.ImmutableSet;
import io.github.rodyamirov.exceptions.UnexpectedTokenException;
import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.lex.Tokenizer;
import io.github.rodyamirov.symbols.Scope;
import io.github.rodyamirov.symbols.TypeSpec;
import io.github.rodyamirov.tree.AndThenNode;
import io.github.rodyamirov.tree.AssignNode;
import io.github.rodyamirov.tree.BinOpNode;
import io.github.rodyamirov.tree.BlockNode;
import io.github.rodyamirov.tree.BooleanConstantNode;
import io.github.rodyamirov.tree.CompoundNode;
import io.github.rodyamirov.tree.DeclarationNode;
import io.github.rodyamirov.tree.DoUntilNode;
import io.github.rodyamirov.tree.ExpressionNode;
import io.github.rodyamirov.tree.IfStatementNode;
import io.github.rodyamirov.tree.IntConstantNode;
import io.github.rodyamirov.tree.NoOpNode;
import io.github.rodyamirov.tree.OrElseNode;
import io.github.rodyamirov.tree.ProcedureCallNode;
import io.github.rodyamirov.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.tree.ProgramNode;
import io.github.rodyamirov.tree.RealConstantNode;
import io.github.rodyamirov.tree.StatementNode;
import io.github.rodyamirov.tree.UnaryOpNode;
import io.github.rodyamirov.tree.VariableAssignNode;
import io.github.rodyamirov.tree.VariableDeclarationNode;
import io.github.rodyamirov.tree.VariableEvalNode;
import io.github.rodyamirov.tree.WhileNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by richard.rast on 12/22/16.
 */
public class Parser {
    public static final Scope ROOT_SCOPE = Scope.makeRootScope(Token.ID("ROOT"));
    private final Tokenizer tokenizer;
    private Token currentToken;
    private Scope currentScope;

    public Parser(String text) {
        this.tokenizer = new Tokenizer(text);
        currentToken = tokenizer.getNextToken();
        currentScope = ROOT_SCOPE;
    }

    private Token eatStrict(Token.Type... types) {
        return eatStrict(type -> inArray(type, types));
    }

    private Optional<Token> eatNonstrict(Token.Type... types) {
        return eatNonstrict(type -> inArray(type, types));
    }

    private boolean inArray(Token.Type type, Token.Type[] array) {
        for (Token.Type elt : array) {
            if (elt == type) {
                return true;
            }
        }
        return false;
    }

    private Token eatStrict(Function<Token.Type, Boolean> predicate) {
        if (predicate.apply(currentToken.type)) {
            Token out = currentToken;
            currentToken = tokenizer.getNextToken();
            return out;
        } else {
            throw UnexpectedTokenException.wrongType(currentToken.type);
        }
    }

    private Optional<Token> eatNonstrict(Function<Token.Type, Boolean> predicate) {
        if (predicate.apply(currentToken.type)) {
            Token out = currentToken;
            currentToken = tokenizer.getNextToken();
            return Optional.of(out);
        } else {
            return Optional.empty();
        }
    }

    public static ProgramNode parseProgram(Scope rootScope, String text) {
        Parser parser = new Parser(text);
        parser.currentScope = rootScope;
        ProgramNode programNode = parser.program();
        parser.eatStrict(Token.Type.EOF);
        return programNode;
    }

    public static ProgramNode parseProgram(String text) {
        return parseProgram(ROOT_SCOPE, text);
    }

    public static ProcedureDeclarationNode parseProcedure(Scope rootScope, String text) {
        Parser parser = new Parser(text);
        parser.currentScope = rootScope;
        ProcedureDeclarationNode out = parser.procedureDeclaration();
        parser.eatStrict(Token.Type.EOF);
        return out;
    }

    public static StatementNode parseStatement(Scope rootScope, String text) {
        Parser parser = new Parser(text);
        parser.currentScope = rootScope;
        StatementNode result = parser.statement();
        parser.eatStrict(Token.Type.EOF);
        return result;
    }

    public static ExpressionNode parseExpression(Scope rootScope, String text) {
        Parser parser = new Parser(text);
        parser.currentScope = rootScope;
        ExpressionNode result = parser.expression();
        parser.eatStrict(Token.Type.EOF);
        return result;
    }

    private ProgramNode program() {
        // program : PROGRAM id SEMI block DOT
        eatStrict(Token.Type.PROGRAM);

        // not sure if this is used anywhere?
        Token<String> programName = (Token<String>) eatStrict(Token.Type.ID);

        // inside the program we're one level down (everything lives inside the program's scope)
        Scope parentScope = currentScope;
        currentScope = currentScope.makeChildScope(programName);

        eatStrict(Token.Type.SEMI);

        BlockNode block = block();

        eatStrict(Token.Type.DOT);

        currentScope = parentScope;

        return new ProgramNode(currentScope, programName, block);
    }

    private BlockNode block() {
        // block -> (declarations)* compoundStatement
        DeclarationNode declarationNode = declaration();

        CompoundNode compoundNode = compoundStatement();

        return new BlockNode(currentScope, declarationNode, compoundNode);
    }

    private DeclarationNode declaration() {
        List<VariableDeclarationNode> declarations = new ArrayList<>();

        if (eatNonstrict(Token.Type.VAR).isPresent()) {
            declarations.add(variableDeclaration());

            while (currentToken.type == Token.Type.ID) {
                declarations.add(variableDeclaration());
            }
        }

        List<ProcedureDeclarationNode> procedures = new ArrayList<>();

        while (currentToken.type == Token.Type.PROCEDURE) {
            procedures.add(procedureDeclaration());
        }

        return new DeclarationNode(currentScope, declarations, procedures);
    }

    private ProcedureDeclarationNode procedureDeclaration() {
        eatStrict(Token.Type.PROCEDURE);
        Token<String> procedureName = eatStrict(Token.Type.ID);

        Scope parentScope = currentScope;
        currentScope = currentScope.makeChildScope(procedureName);

        eatStrict(Token.Type.SEMI);
        BlockNode blockNode = block();
        eatStrict(Token.Type.SEMI);

        currentScope = parentScope;

        return new ProcedureDeclarationNode(currentScope, procedureName, blockNode);
    }

    private VariableDeclarationNode variableDeclaration() {
        // note that VAR is consumed elsewhere so we don't check it here
        List<Token<String>> ids = new ArrayList<>();

        ids.add(eatStrict(Token.Type.ID));

        while (eatNonstrict(Token.Type.COMMA).isPresent()) {
            ids.add(eatStrict(Token.Type.ID));
        }

        eatStrict(Token.Type.COLON);

        TypeSpec typeSpec = typeSpec();

        eatStrict(Token.Type.SEMI);

        return new VariableDeclarationNode(currentScope, ids, typeSpec);
    }

    private TypeSpec typeSpec() {
        Token typeToken = eatStrict(Token.Type.VAR_TYPE);
        return (TypeSpec) typeToken.value;
    }

    private CompoundNode compoundStatement() {
        // compoundStatement -> BEGIN statementList END
        eatStrict(Token.Type.BEGIN);

        List<StatementNode> statements = statementList();

        eatStrict(Token.Type.END);

        return new CompoundNode(currentScope, statements);
    }

    private List<StatementNode> statementList() {
        // statementList -> statement (SEMI statement)*
        List<StatementNode> out = new ArrayList<>();

        out.add(statement());

        while (eatNonstrict(Token.Type.SEMI).isPresent()) {
            out.add(statement());
        }

        return out;
    }

    private StatementNode statement() {
        // statement -> compoundStatement | ifStatement | assignmentStatement | empty
        switch (currentToken.type) {
            case DO:
                return doUntilStatement();
            case WHILE:
                return whileStatement();
            case BEGIN:
                return compoundStatement();
            case IF:
                return ifStatement();
            case ID:
                Token.Type nextType = tokenizer.peek().type;
                switch (nextType) {
                    case ASSIGN:
                        return assignmentStatement();

                    case L_PAREN:
                        return procedureCallStatement();

                    default:
                        throw UnexpectedTokenException.wrongType(nextType);
                }
            default:
                return empty();
        }
    }

    private DoUntilNode doUntilStatement() {
        eatStrict(Token.Type.DO);

        StatementNode childStatement = statement();

        eatStrict(Token.Type.UNTIL);

        ExpressionNode condition = expression();

        return new DoUntilNode(currentScope, condition, childStatement);
    }

    private WhileNode whileStatement() {
        eatStrict(Token.Type.WHILE);

        ExpressionNode condition = expression();

        eatStrict(Token.Type.DO);

        StatementNode childStatement = statement();

        return new WhileNode(currentScope, condition, childStatement);
    }

    private ProcedureCallNode procedureCallStatement() {
        Token<String> procedureId = eatStrict(Token.Type.ID);

        // currently no arguments are accepted
        eatStrict(Token.Type.L_PAREN);
        eatStrict(Token.Type.R_PAREN);

        return new ProcedureCallNode(currentScope, procedureId);
    }

    private IfStatementNode ifStatement() {
        eatStrict(Token.Type.IF);

        ExpressionNode condition = expression();

        eatStrict(Token.Type.THEN);

        StatementNode thenStatement = statement();

        if (eatNonstrict(Token.Type.ELSE).isPresent()) {
            StatementNode elseStatement = statement();
            return new IfStatementNode(currentScope, condition, thenStatement, elseStatement);
        } else {
            return new IfStatementNode(currentScope, condition, thenStatement);
        }
    }

    private StatementNode assignmentStatement() {
        // assignmentStatement -> variable ASSIGN additiveExpression
        VariableAssignNode var = variableDefinition();
        eatStrict(Token.Type.ASSIGN);
        ExpressionNode right = expression();

        return new AssignNode(currentScope, var, right);
    }

    private VariableAssignNode variableDefinition() {
        Token id = eatStrict(Token.Type.ID);
        return new VariableAssignNode(currentScope, id);
    }

    private VariableEvalNode variable() {
        // variable -> ID
        Token id = eatStrict(Token.Type.ID);
        return new VariableEvalNode(currentScope, id);
    }

    private StatementNode empty() {
        return new NoOpNode(currentScope);
    }

    private ExpressionNode expression() {
        // expr -> comp ([and then | or else] comp)*
        ExpressionNode out = compareExpression();

        // sadly the functional style doesn't work well here
        // still doing left-associativity
        boolean hasMore = true;
        while (hasMore) {
            // if we see an AND, it didn't grab at a lower level, which means
            // it must be an AND-THEN (and likewise with OR-ELSE)
            if (eatNonstrict(Token.Type.AND).isPresent()) {
                eatStrict(Token.Type.THEN);
                out = new AndThenNode(currentScope, out, compareExpression());
            } else if (eatNonstrict(Token.Type.OR).isPresent()) {
                eatStrict(Token.Type.ELSE);
                out = new OrElseNode(currentScope, out, compareExpression());
            } else {
                hasMore = false;
            }
        }

        return out;
    }

    private static final ImmutableSet<Token.Type> compareTypes = ImmutableSet.<Token.Type>builder()
            .add(Token.Type.EQUALS).add(Token.Type.NOT_EQUALS).add(Token.Type.LESS_THAN)
            .add(Token.Type.LESS_THAN_OR_EQUALS).add(Token.Type.GREATER_THAN)
            .add(Token.Type.GREATER_THAN_OR_EQUALS).build();

    /**
     * The lowest-precedence operator binding (and thus the "highest" node in terms of being
     * the first one you go to, to get an "expression")
     * @return
     */
    private ExpressionNode compareExpression() {
        ExpressionNode out = additiveExpression();

        // this formulation gets left associativity correct; obvious recursion does not
        Optional<Token> maybeOpToken;
        while ((maybeOpToken = eatNonstrict(compareTypes::contains)).isPresent()) {
            Token opToken = maybeOpToken.get();
            out = new BinOpNode(currentScope, out, additiveExpression(), opToken);
        }

        return out;
    }

    private static final ImmutableSet<Token.Type> expressionTypes = ImmutableSet.<Token.Type>builder()
            .add(Token.Type.PLUS).add(Token.Type.MINUS).build();

    private ExpressionNode additiveExpression() {
        // additiveExpression -> factor ([+-] factor)*
        // additiveExpression -> factor | factor [+-] additiveExpression

        ExpressionNode out = factor();

        // this formulation gets left associativity correct; obvious recursion does not
        boolean keepGoing = true;
        while (keepGoing) {
            Optional<Token> maybeOpToken;
            if ((maybeOpToken = eatNonstrict(expressionTypes::contains)).isPresent()) {
                Token opToken = maybeOpToken.get();
                out = new BinOpNode(currentScope, out, factor(), opToken);
            } else if (currentToken.equals(Token.OR) && ! tokenizer.peek().equals(Token.ELSE)) {
                eatStrict(Token.Type.OR);
                out = new BinOpNode(currentScope, out, factor(), Token.OR);
            } else {
                keepGoing = false;
            }
        }

        return out;
    }

    private static final ImmutableSet<Token.Type> factorTypes = ImmutableSet.<Token.Type>builder()
            .add(Token.Type.TIMES).add(Token.Type.REAL_DIVIDE).add(Token.Type.INT_DIVIDE)
            .add(Token.Type.MOD).build();

    private ExpressionNode factor() {
        // factor -> terminal ([*/ AND] terminal)*
        // factor -> terminal | terminal [*/ AND] factor

        ExpressionNode out = unop();

        // this formulation gets left associativity correct; obvious recursion does not
        boolean keepGoing = true;
        while (keepGoing) {
            Optional<Token> maybeOpToken;
            if ((maybeOpToken = eatNonstrict(factorTypes::contains)).isPresent()) {
                Token opToken = maybeOpToken.get();
                out = new BinOpNode(currentScope, out, unop(), opToken);
            } else if (currentToken.equals(Token.AND) && ! tokenizer.peek().equals(Token.THEN)) {
                eatStrict(Token.Type.AND);
                out = new BinOpNode(currentScope, out, unop(), Token.AND);
            } else {
                keepGoing = false;
            }
        }

        return out;
    }

    private static final ImmutableSet<Token.Type> unopTypes = ImmutableSet.<Token.Type>builder()
            .add(Token.Type.PLUS).add(Token.Type.MINUS).add(Token.Type.NOT).build();

    private ExpressionNode unop() {
        // unop -> ([+-NOT])* terminal
        // unop -> terminal | [+-NOT] unop

        // unops are right associative (in a sense) so recursion handles it well
        Optional<Token> maybeOpToken;
        if ((maybeOpToken = eatNonstrict(unopTypes::contains)).isPresent()) {
            Token opToken = maybeOpToken.get();
            return new UnaryOpNode(currentScope, unop(), opToken);
        } else {
            return terminal();
        }
    }

    private ExpressionNode terminal() {
        // terminal -> INTEGER | REAL | BOOLEAN | variable | L_PAREN expr R_PAREN

        Optional<Token> maybeToken;
        if ((maybeToken = eatNonstrict(Token.Type.INTEGER_CONSTANT)).isPresent()) {
            Token intToken = maybeToken.get();
            return IntConstantNode.make(currentScope, intToken);
        } else if ((maybeToken = eatNonstrict(Token.Type.REAL_CONSTANT)).isPresent()) {
            Token realToken = maybeToken.get();
            return RealConstantNode.make(currentScope, realToken);
        } else if ((maybeToken = eatNonstrict(Token.Type.BOOLEAN_CONSTANT)).isPresent()) {
            Token boolToken = maybeToken.get();
            return BooleanConstantNode.make(currentScope, boolToken);
        } else if ((maybeToken = eatNonstrict(Token.Type.ID)).isPresent()) {
            Token variableToken = maybeToken.get();
            return new VariableEvalNode(currentScope, variableToken);
        } else {
            eatStrict(Token.Type.L_PAREN);
            ExpressionNode out = expression();
            eatStrict(Token.Type.R_PAREN);
            return out;
        }
    }
}
