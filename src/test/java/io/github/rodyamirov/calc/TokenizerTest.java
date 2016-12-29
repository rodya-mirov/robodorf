package io.github.rodyamirov.calc;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/22/16.
 */
public class TokenizerTest {
    private void getTokensTest(String text, Token[] correct) {
        Tokenizer tokenizer = new Tokenizer(text);

        for (int i = 0; i < correct.length; i++) {
            Token actual = tokenizer.getNextToken();
            Token desired = correct[i];
            assertThat("Got the intended token", actual, is(desired));
        }
    }

    @Test
    public void simpleTest() {
        String text = "";
        Token[] correct = new Token[] { Token.EOF, Token.EOF, Token.EOF }; // and so on

        getTokensTest(text, correct);
    }

    @Test
    public void whitespaceTest() {
        String text = "    \t \n  \r";
        Token[] correct = new Token[] { Token.EOF, Token.EOF, Token.EOF }; // and so on

        getTokensTest(text, correct);
    }

    @Test
    public void numbersTest() {
        String text = "   12  14  11  1\t";
        Token[] correct = new Token[] {
                Token.INT(12), Token.INT(14), Token.INT(11), Token.INT(1),
                Token.EOF, Token.EOF, Token.EOF }; // and so on

        getTokensTest(text, correct);
    }

    @Test
    public void operatorsTest() {
        String text = "+*-/++//";
        Token[] correct = new Token[] {
                Token.PLUS, Token.TIMES, Token.MINUS, Token.DIVIDE, Token.PLUS,
                Token.PLUS, Token.DIVIDE, Token.DIVIDE,
                Token.EOF, Token.EOF, Token.EOF }; // and so on

        getTokensTest(text, correct);
    }

    @Test
    public void mixedTest() {
        String text = "\t12- 13++\n-/\t1- 5";
        Token[] correct = new Token[] {
                Token.INT(12), Token.MINUS, Token.INT(13), Token.PLUS, Token.PLUS,
                Token.MINUS, Token.DIVIDE, Token.INT(1), Token.MINUS, Token.INT(5),
                Token.EOF, Token.EOF, Token.EOF }; // and so on

        getTokensTest(text, correct);
    }

    @Test
    public void parenTest() {
        String text = "\t(()((()())))(((\n\t(";

        Token[] correct = new Token[] {
                Token.L_PAREN, Token.L_PAREN, Token.R_PAREN, Token.L_PAREN,
                Token.L_PAREN, Token.L_PAREN, Token.R_PAREN, Token.L_PAREN,
                Token.R_PAREN, Token.R_PAREN, Token.R_PAREN, Token.R_PAREN,
                Token.L_PAREN, Token.L_PAREN, Token.L_PAREN, Token.L_PAREN,
                Token.EOF
        };

        getTokensTest(text, correct);
    }
}
