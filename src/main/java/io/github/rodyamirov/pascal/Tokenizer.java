package io.github.rodyamirov.pascal;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

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
        reservedWordsMap.put(standardizeId("DIV"), Token.INT_DIVIDE);
        reservedWordsMap.put(standardizeId("PROGRAM"), Token.PROGRAM);
        reservedWordsMap.put(standardizeId("INTEGER"), Token.INT_TYPE);
        reservedWordsMap.put(standardizeId("REAL"), Token.REAL_TYPE);
        reservedWordsMap.put(standardizeId("VAR"), Token.VAR);
        reservedWordsMap.put(standardizeId("PROCEDURE"), Token.PROCEDURE);
        reservedWordsMap.put(standardizeId("MOD"), Token.MOD);
        reservedWords = ImmutableMap.copyOf(reservedWordsMap);
    }

    public static String standardizeId(String id) {
        // for whatever reason, pascal ids are case insensitive, so to avoid this annoyance we
        // just standardize them during the lexer so nobody later has to care
        return id.toLowerCase();
    }

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

    public Token getNextToken() {
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

            case ':':
                if (pos+1 < text.length() && text.charAt(pos+1) == '=') {
                    advance();
                    advance();
                    return Token.ASSIGN;
                } else {
                    advance();
                    return Token.COLON;
                }

            default:
                String message = String.format("Cannot parse character %s at index %s", c, pos);
                throw new IllegalStateException(message);
        }
    }
}
