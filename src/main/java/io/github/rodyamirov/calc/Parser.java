package io.github.rodyamirov.calc;

import io.github.rodyamirov.calc.tree.BinOpNode;
import io.github.rodyamirov.calc.tree.ConstantNode;
import io.github.rodyamirov.calc.tree.SyntaxTree;
import io.github.rodyamirov.calc.tree.UnaryOpNode;

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

    public SyntaxTree parse() {
        // expr -> addExpr
        SyntaxTree<Integer> result = addExpr();

        // just an assertion that we completed the text!
        eatStrict(Token.Type.EOF);

        return result;
    }

    private SyntaxTree<Integer> addExpr() {
        // addExpr -> mulExpr ( [+-] addExpr)*
        // TODO: unroll the recursion, because gross
        SyntaxTree<Integer> result = mulExpr();

        // functional stylllllle
        Optional<Token> maybeOp;

        // assign and condition to avoid code reuse
        while ((maybeOp = eatNonstrict(Token.Type.PLUS, Token.Type.MINUS)).isPresent()) {
            result = new BinOpNode(result, mulExpr(), maybeOp.get());
        }

        return result;
    }

    private SyntaxTree<Integer> mulExpr() {
        // mulExpr -> unaryExpr ( [*/] mulExpr )*
        // TODO: unroll the recursion, because gross
        SyntaxTree<Integer> result = unaryExpr();

        // dem functionz
        Optional<Token> maybeOp;

        while ((maybeOp = eatNonstrict(Token.Type.TIMES, Token.Type.DIVIDE)).isPresent()) {
            result = new BinOpNode(result, unaryExpr(), maybeOp.get());
        }

        return result;
    }

    private SyntaxTree<Integer> unaryExpr() {
        // unaryExpr -> [+-]* bottomExpr
        // these are right-associative so the "natural" recursive approach works correctly

        Optional<Token> maybeOp = eatNonstrict(Token.Type.PLUS, Token.Type.MINUS);

        if (maybeOp.isPresent()) {
            return new UnaryOpNode(unaryExpr(), maybeOp.get());
        } else {
            return bottomExpr();
        }
    }

    private SyntaxTree<Integer> bottomExpr() {
        // bottomExpr -> INT | LPAREN addExpr RPAREN
        Token token = eatStrict(Token.Type.INT, Token.Type.L_PAREN);

        switch (token.type) {
            case L_PAREN:
                SyntaxTree<Integer> result = addExpr();
                eatStrict(Token.Type.R_PAREN);
                return result;

            case INT:
                return new ConstantNode((int)token.value);

            default:
                String message = String.format("Unexpected type %s; expected integer or L_PAREN", token.type);
                throw new IllegalStateException(message);
        }
    }
}
