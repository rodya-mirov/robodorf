package io.github.rodyamirov.calc;

/**
 * Created by richard.rast on 12/22/16.
 */
public class Tokenizer {
    private final String text;
    private int pos;

    public Tokenizer(String text) {
        this.text = text;
        this.pos = 0;
    }

    private boolean atEnd() {
        return this.pos >= this.text.length();
    }

    private void advance() {
        if (this.pos < this.text.length()) {
            this.pos += 1;
        }
    }

    /**
     * @throws IndexOutOfBoundsException if atEnd() is true
     */
    private char currentChar() {
        return this.text.charAt(pos);
    }

    private void skipWhitespace() {
        while (!atEnd() && Character.isWhitespace(currentChar())) {
            advance();
        }
    }

    private Token<Integer> makeInteger() {
        StringBuilder sb = new StringBuilder();

        do {
            sb.append(currentChar());
            advance();
        } while (!atEnd() && Character.isDigit(currentChar()));

        int value = Integer.parseInt(sb.toString());
        return Token.INT(value);
    }

    public Token getNextToken() {
        skipWhitespace();

        if (atEnd()) {
            return Token.EOF;
        } else if (Character.isDigit(currentChar())) {
            return makeInteger();
        } else switch (currentChar()) {
            case '+':
                advance();
                return Token.PLUS;
            case '-':
                advance();
                return Token.MINUS;
            case '*':
                advance();
                return Token.TIMES;
            case '/':
                advance();
                return Token.DIVIDE;
            case '(':
                advance();
                return Token.L_PAREN;
            case ')':
                advance();
                return Token.R_PAREN;
            default:
                throw new IllegalStateException("Unrecognized character " + currentChar());
        }
    }
}
