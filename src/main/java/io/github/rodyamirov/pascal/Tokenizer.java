package io.github.rodyamirov.pascal;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by richard.rast on 12/22/16.
 */
public class Tokenizer {
    // rig up our keywords ...
    private static final ImmutableMap<String, Token> reservedWords;
    static {
        Map<String, Token> reservedWordsMap = new HashMap<>();
        reservedWordsMap.put(standardizeId("BEGIN"), Token.BEGIN);
        reservedWordsMap.put(standardizeId("END"), Token.END);
        reservedWordsMap.put(standardizeId("PROGRAM"), Token.PROGRAM);
        reservedWordsMap.put(standardizeId("PROCEDURE"), Token.PROCEDURE);
        reservedWordsMap.put(standardizeId("VAR"), Token.VAR);

        reservedWordsMap.put(standardizeId("INTEGER"), Token.INTEGER_TYPE);
        reservedWordsMap.put(standardizeId("REAL"), Token.REAL_TYPE);
        reservedWordsMap.put(standardizeId("BOOLEAN"), Token.BOOLEAN_TYPE);

        reservedWordsMap.put(standardizeId("TRUE"), Token.TRUE);
        reservedWordsMap.put(standardizeId("FALSE"), Token.FALSE);
        reservedWordsMap.put(standardizeId("AND"), Token.AND);
        reservedWordsMap.put(standardizeId("THEN"), Token.THEN);
        reservedWordsMap.put(standardizeId("OR"), Token.OR);
        reservedWordsMap.put(standardizeId("ELSE"), Token.ELSE);
        reservedWordsMap.put(standardizeId("NOT"), Token.NOT);
        reservedWordsMap.put(standardizeId("IF"), Token.IF);

        reservedWordsMap.put(standardizeId("DIV"), Token.INT_DIVIDE);
        reservedWordsMap.put(standardizeId("MOD"), Token.MOD);
        reservedWords = ImmutableMap.copyOf(reservedWordsMap);
    }

    public static String standardizeId(String id) {
        // for whatever reason, pascal ids are case insensitive, so to avoid this annoyance we
        // just standardize them during the lexer so nobody later has to care
        return id.toLowerCase();
    }

    private final LinkedList<Token> tokenQueue = new LinkedList<>();
    private final String text;
    private int pos;

    public Tokenizer(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }

        this.text = text;
        this.pos = 0;
    }

    private boolean atEnd() {
        return pos >= text.length();
    }

    private void advance() {
        if (pos <= text.length()) {
            pos += 1;
        }
    }

    private char getCurrentCharacter() {
        return text.charAt(pos);
    }

    private void skipWhitespace() {
        // no PRE: always safe to call
        while (!atEnd() && Character.isWhitespace(getCurrentCharacter())) {
            advance();
        }
    }

    private void skipComment() {
        // no PRE
        if (!atEnd() && getCurrentCharacter() == '{') {
            int openCount = 1;
            advance();

            while (!atEnd() && openCount > 0) {
                switch (getCurrentCharacter()) {
                    case '{': openCount += 1; break;
                    case '}': openCount -= 1; break;
                    default: break; // just the inside of a comment
                }
                advance();
            }

            if (openCount > 0) {
                String message = String.format(
                        "EOF reached, still %d comment levels deep", openCount
                );
                throw new IllegalStateException(message);
            } // otherwise all good
        }
    }

    private boolean isIdInternalCharacter(char c) {
        return c == '_' || Character.isLetterOrDigit(c);
    }

    private Token getId() {
        // PRE: assumes getCurrentCharacter is _ or an alphabetical character
        StringBuilder sb = new StringBuilder(String.valueOf(getCurrentCharacter()));

        advance();

        while (!atEnd() && isIdInternalCharacter(getCurrentCharacter())) {
            sb.append(getCurrentCharacter());
            advance();
        }

        // for whatever reason, ids are case insensitive, so we just standardize them
        // during the lexing and everybody's happy
        String tokenString = standardizeId(sb.toString());

        for (String reservedKey : reservedWords.keySet()) {
            if (reservedKey.equals(tokenString)) {
                return reservedWords.get(reservedKey);
            }
        }

        return Token.ID(tokenString);
    }

    private Token getNumericalConstant() {
        // PRE: assumes getCurrentCharacter is a digit
        StringBuilder sb = new StringBuilder(String.valueOf(getCurrentCharacter()));

        advance();

        while (!atEnd() && Character.isDigit(getCurrentCharacter())) {
            sb.append(getCurrentCharacter());
            advance();
        }

        // check if this is actually a float
        if (!atEnd() && getCurrentCharacter() == '.') {
            sb.append('.');
            advance();

            while (!atEnd() && Character.isDigit(getCurrentCharacter())) {
                sb.append(getCurrentCharacter());
                advance();
            }

            return Token.REAL_CONSTANT(Float.parseFloat(sb.toString()));
        } else {
            return Token.INT_CONSTANT(Integer.parseInt(sb.toString()));
        }
    }

    /**
     * Returns the next token in the text, advancing the internal counter as it does so.
     * Iterating `getNextToken` repeatedly will return each Token in the text exactly once,
     * then return EOF indefinitely when the text is expended.
     *
     * @return The next token in the text
     */
    public Token getNextToken() {
        if (tokenQueue.size() == 0) {
            tokenQueue.add(makeToken());
        }

        return tokenQueue.poll();
    }

    /**
     * Alias for peek(0).
     *
     * Shows what the next token in the text would be, if getNextToken were called. This does
     * not have externally visible side effects, so calling it repeatedly is perfectly safe.
     * Additionally, calling peek does not affect the next call of getNextToken, and does not
     * incur any performance penalty.
     *
     * @return The next token in the text
     */
    public Token peek() {
        return peek(0);
    }

    /**
     * Shows what the next-plus-skip token in the text would be, if getNextToken were called.
     * This does not have externally visible side effects, so calling it repeatedly is perfectly
     * safe. Additionally, calling peek does not affect the next call of getNextToken, and does not
     * incur any performance penalty.
     *
     * @param skip The number of tokens to skip ahead for the peek
     * @return The next-plus-skip token in the text
     */
    public Token peek(int skip) {
        while (tokenQueue.size() <= skip) {
            tokenQueue.add(makeToken());
        }

        return tokenQueue.get(skip);
    }

    private Token makeToken() {
        if (atEnd()) {
            return Token.EOF;
        }

        char c = getCurrentCharacter();

        while (c == '{' || Character.isWhitespace(c)) {
            skipComment();
            skipWhitespace();

            if (atEnd()) {
                return Token.EOF;
            } else {
                c = getCurrentCharacter();
            }
        }

        if (Character.isAlphabetic(c) || c == '_') {
            return getId();
        }

        if (Character.isDigit(c)) {
            return getNumericalConstant();
        }

        if (Character.isWhitespace(c)) {
            skipWhitespace();
            return getNextToken();
        }

        switch (c) {
            case '+': advance(); return Token.PLUS;
            case '-': advance(); return Token.MINUS;
            case '*': advance(); return Token.TIMES;
            case '/': advance(); return Token.REAL_DIVIDE;

            case '(': advance(); return Token.L_PAREN;
            case ')': advance(); return Token.R_PAREN;

            case ';': advance(); return Token.SEMI;
            case '.': advance(); return Token.DOT;

            case ',': advance(); return Token.COMMA;

            case '=': advance(); return Token.EQUALS;

            case '<':
                if (pos+1 < text.length() && text.charAt(pos+1) == '>') {
                    advance(); advance(); return Token.NOT_EQUALS;
                } else if (pos+1 < text.length() && text.charAt(pos+1) == '=') {
                    advance(); advance(); return Token.LESS_THAN_OR_EQUALS;
                } else {
                    advance(); return Token.LESS_THAN;
                }

            case '>':
                if (pos + 1 < text.length() && text.charAt(pos+1) == '=') {
                    advance(); advance(); return Token.GREATER_THAN_OR_EQUALS;
                } else {
                    advance(); return Token.GREATER_THAN;
                }

            case ':':
                if (pos+1 < text.length() && text.charAt(pos+1) == '=') {
                    advance(); advance(); return Token.ASSIGN;
                } else {
                    advance(); return Token.COLON;
                }

            default:
                String message = String.format("Cannot parse character %s at index %s", c, pos);
                throw new IllegalStateException(message);
        }
    }
}
