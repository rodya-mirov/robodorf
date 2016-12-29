package io.github.rodyamirov.pascal;

import io.github.rodyamirov.pascal.tree.AssignNode;
import io.github.rodyamirov.pascal.tree.BinOpNode;
import io.github.rodyamirov.pascal.tree.BlockNode;
import io.github.rodyamirov.pascal.tree.CompoundNode;
import io.github.rodyamirov.pascal.tree.DeclarationNode;
import io.github.rodyamirov.pascal.tree.ExpressionNode;
import io.github.rodyamirov.pascal.tree.IntConstantNode;
import io.github.rodyamirov.pascal.tree.NoOpNode;
import io.github.rodyamirov.pascal.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.pascal.tree.ProgramNode;
import io.github.rodyamirov.pascal.tree.RealConstantNode;
import io.github.rodyamirov.pascal.tree.StatementNode;
import io.github.rodyamirov.pascal.tree.UnaryOpNode;
import io.github.rodyamirov.pascal.tree.VariableAssignNode;
import io.github.rodyamirov.pascal.tree.VariableDeclarationNode;
import io.github.rodyamirov.pascal.tree.VariableEvalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by richard.rast on 12/22/16.
 */
public class Parser {
    private final Tokenizer tokenizer;
    private Token currentToken;

    public Parser(String text) {
        this.tokenizer = new Tokenizer(text);
        currentToken = tokenizer.getNextToken();
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
            String message = String.format("Cannot accept type %s", currentToken.type);
            throw new IllegalStateException(message);
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

    public ProgramNode parseProgram() {
        ProgramNode result = program();

        // just an assertion that we completed the text!
        eatStrict(Token.Type.EOF);

        return result;
    }

    public ExpressionNode parseExpression() {
        ExpressionNode result = expression();

        // just an assertion that we completed the text!
        eatStrict(Token.Type.EOF);

        return result;
    }

    private ProgramNode program() {
        // program : PROGRAM id SEMI block DOT
        eatStrict(Token.Type.PROGRAM);

        // not sure if this is used anywhere?
        Token<String> programName = (Token<String>) eatStrict(Token.Type.ID);

        eatStrict(Token.Type.SEMI);

        BlockNode block = block();

        eatStrict(Token.Type.DOT);

        return new ProgramNode(programName, block);
    }

    private BlockNode block() {
        // block -> (declarations)* compoundStatement
        DeclarationNode declarationNode = declaration();

        CompoundNode compoundNode = compoundStatement();

        return new BlockNode(declarationNode, compoundNode);
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

        while (eatNonstrict(Token.Type.PROCEDURE).isPresent()) {
            Token<String> procedureName = eatStrict(Token.Type.ID);
            eatStrict(Token.Type.SEMI);
            BlockNode blockNode = block();
            eatStrict(Token.Type.SEMI);

            procedures.add(new ProcedureDeclarationNode(procedureName, blockNode));
        }

        return new DeclarationNode(declarations, procedures);
    }

    private VariableDeclarationNode variableDeclaration() {
        List<Token<String>> ids = new ArrayList<>();

        ids.add(eatStrict(Token.Type.ID));

        while (eatNonstrict(Token.Type.COMMA).isPresent()) {
            ids.add(eatStrict(Token.Type.ID));
        }

        eatStrict(Token.Type.COLON);

        TypeSpec typeSpec = typeSpec();

        eatStrict(Token.Type.SEMI);

        return new VariableDeclarationNode(ids, typeSpec);
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

        return new CompoundNode(statements);
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
        // statement -> compoundStatement | assignmentStatement | empty
        switch (currentToken.type) {
            case BEGIN:
                return compoundStatement();
            case ID:
                return assignmentStatement();
            default:
                return empty();
        }
    }

    private StatementNode assignmentStatement() {
        // assignmentStatement -> variable ASSIGN expression
        VariableAssignNode var = variableDefinition();
        eatStrict(Token.Type.ASSIGN);
        ExpressionNode right = expression();

        return new AssignNode(var, right);
    }

    private VariableAssignNode variableDefinition() {
        Token id = eatStrict(Token.Type.ID);
        return new VariableAssignNode(id);
    }

    private VariableEvalNode variable() {
        // variable -> ID
        Token id = eatStrict(Token.Type.ID);
        return new VariableEvalNode(id);
    }

    private StatementNode empty() {
        return new NoOpNode();
    }

    private ExpressionNode expression() {
        // expression -> factor ([+-] factor)*
        // expression -> factor | factor [+-] expression

        ExpressionNode out = factor();

        // this formulation gets left associativity correct; obvious recursion does not
        Optional<Token> maybeOpToken;
        while ((maybeOpToken = eatNonstrict(Token.Type.PLUS, Token.Type.MINUS)).isPresent()) {
            Token opToken = maybeOpToken.get();
            out = new BinOpNode(out, factor(), opToken);
        }

        return out;
    }

    private ExpressionNode factor() {
        // factor -> terminal ([*/] terminal)*
        // factor -> terminal | terminal [*/] factor

        ExpressionNode out = unop();

        // this formulation gets left associativity correct; obvious recursion does not
        Optional<Token> maybeOpToken;
        while ((maybeOpToken = eatNonstrict(Token.Type.TIMES, Token.Type.REAL_DIVIDE, Token.Type.INT_DIVIDE, Token.Type.MOD)).isPresent()) {
            Token opToken = maybeOpToken.get();
            out = new BinOpNode(out, unop(), opToken);
        }

        return out;
    }

    private ExpressionNode unop() {
        // unop -> ([+-])* terminal
        // unop -> terminal | [+-] unop

        // unops are right associative (in a sense) so recursion handles it well
        Optional<Token> maybeOpToken;
        if ((maybeOpToken = eatNonstrict(Token.Type.PLUS, Token.Type.MINUS)).isPresent()) {
            Token opToken = maybeOpToken.get();
            return new UnaryOpNode(unop(), opToken);
        } else {
            return terminal();
        }
    }

    private ExpressionNode terminal() {
        // terminal -> INTEGER | REAL | variable | L_PAREN expr R_PAREN

        Optional<Token> maybeToken;
        if ((maybeToken = eatNonstrict(Token.Type.INT_CONSTANT)).isPresent()) {
            Token intToken = maybeToken.get();
            return IntConstantNode.make(intToken);
        } else if ((maybeToken = eatNonstrict(Token.Type.REAL_CONSTANT)).isPresent()) {
            Token realToken = maybeToken.get();
            return RealConstantNode.make(realToken);
        } else if ((maybeToken = eatNonstrict(Token.Type.ID)).isPresent()) {
            Token variableToken = maybeToken.get();
            return new VariableEvalNode(variableToken);
        } else {
            eatStrict(Token.Type.L_PAREN);
            ExpressionNode out = expression();
            eatStrict(Token.Type.R_PAREN);
            return out;
        }
    }
}
