package io.github.rodyamirov.pascal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by richard.rast on 12/22/16.
 */
public class TokenizerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private void doTokenizerTest(String text, Token[] tokens) {
        Tokenizer tokenizer = new Tokenizer(text);
        for (Token token : tokens) {
            assertThat(tokenizer.getNextToken(), is(token));
        }
    }

    private void doTokenizerTestThenError(String text, Token[] tokens, String errorMessage) {
        Tokenizer tokenizer = new Tokenizer(text);

        // check we get the expected tokens while they exist
        for (Token token : tokens) {
            assertThat(tokenizer.getNextToken(), is(token));
        }

        // then get an error
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(is(errorMessage));

        tokenizer.getNextToken();
    }

    @Test
    public void tokenizerTest1() {
        String text = "BEGIN 1 2 3 1 - 1 +\t 1276 END ; := yUUU _poE1e";
        Token[] tokens = new Token[] {
                Token.BEGIN, Token.INT_CONSTANT(1), Token.INT_CONSTANT(2), Token.INT_CONSTANT(3),
                Token.INT_CONSTANT(1), Token.MINUS, Token.INT_CONSTANT(1), Token.PLUS,
                Token.INT_CONSTANT(1276), Token.END, Token.SEMI, Token.ASSIGN,
                Token.ID("yUUU"), Token.ID("_poE1e"), Token.EOF, Token.EOF,
                Token.EOF, Token.EOF, Token.EOF, Token.EOF, Token.EOF // and so on
        };

        doTokenizerTest(text, tokens);
    }

    @Test
    public void tokenizerTest2() {
        String text = "dbshdb _d21dn1 _ 12 -*+-/+12-12 div dIv DiV EN END enD _eNd BEGIN bEgIn . .... .";

        Token[] tokens = new Token[] {
                Token.ID("dbshdb"), Token.ID("_d21dn1"), Token.ID("_"),
                Token.INT_CONSTANT(12), Token.MINUS, Token.TIMES, Token.PLUS, Token.MINUS,
                Token.REAL_DIVIDE, Token.PLUS, Token.INT_CONSTANT(12), Token.MINUS, Token.INT_CONSTANT(12),
                Token.INT_DIVIDE, Token.INT_DIVIDE, Token.INT_DIVIDE,
                Token.ID("EN"), Token.END, Token.END, Token.ID("_eNd"),
                Token.BEGIN, Token.BEGIN, Token.DOT, Token.DOT,
                Token.DOT, Token.DOT, Token.DOT, Token.DOT,
                Token.EOF, Token.EOF, Token.EOF, Token.EOF, Token.EOF, Token.EOF,
                Token.EOF, Token.EOF, Token.EOF, Token.EOF, Token.EOF, Token.EOF
        };

        doTokenizerTest(text, tokens);
    }

    @Test
    public void tokenizerTest3() {
        String text = "-1423 :,: -";
        Token[] correct = new Token[] {
                Token.MINUS, Token.INT_CONSTANT(1423),
                Token.COLON, Token.COMMA, Token.COLON,
                Token.MINUS, Token.EOF
        };

        doTokenizerTest(text, correct);

        text = "+1423,var 2,prOgram PROCedURe pROcEDure 1,";
        correct = new Token[] {
                Token.PLUS, Token.INT_CONSTANT(1423),
                Token.COMMA, Token.VAR, Token.INT_CONSTANT(2), Token.COMMA,
                Token.PROGRAM, Token.PROCEDURE, Token.PROCEDURE, Token.INT_CONSTANT(1), Token.COMMA,
                Token.EOF
        };

        doTokenizerTest(text, correct);


    }

    @Test
    public void simpleTest() {
        String text = "";
        Token[] correct = new Token[] { Token.EOF, Token.EOF, Token.EOF }; // and so on

        doTokenizerTest(text, correct);
    }

    @Test
    public void whitespaceTest() {
        String text = "    \t \n  \r";
        Token[] correct = new Token[] { Token.EOF, Token.EOF, Token.EOF }; // and so on

        doTokenizerTest(text, correct);
    }

    @Test
    public void numbersTest() {
        String text = "  11.12 12  14  11  1\t";
        Token[] correct = new Token[] {
                Token.REAL_CONSTANT(11.12f),
                Token.INT_CONSTANT(12), Token.INT_CONSTANT(14),
                Token.INT_CONSTANT(11), Token.INT_CONSTANT(1),
                Token.EOF, Token.EOF, Token.EOF }; // and so on

        doTokenizerTest(text, correct);
    }

    @Test
    public void operatorsTest() {
        String text = "+*div-/+DiV+//";
        Token[] correct = new Token[] {
                Token.PLUS, Token.TIMES, Token.INT_DIVIDE, Token.MINUS, Token.REAL_DIVIDE, Token.PLUS,
                Token.INT_DIVIDE, Token.PLUS, Token.REAL_DIVIDE, Token.REAL_DIVIDE,
                Token.EOF, Token.EOF, Token.EOF }; // and so on

        doTokenizerTest(text, correct);
    }

    @Test
    public void mixedTest() {
        String text = "\t12- 13+DIV+\n-/\t1- 5";
        Token[] correct = new Token[] {
                Token.INT_CONSTANT(12), Token.MINUS, Token.INT_CONSTANT(13),
                Token.PLUS, Token.INT_DIVIDE, Token.PLUS,
                Token.MINUS, Token.REAL_DIVIDE, Token.INT_CONSTANT(1),
                Token.MINUS, Token.INT_CONSTANT(5),
                Token.EOF, Token.EOF, Token.EOF }; // and so on

        doTokenizerTest(text, correct);
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

        doTokenizerTest(text, correct);
    }

    @Test
    public void commentTest1() {
        String text = "1{2 3}4";

        Token[] correct = new Token[] {
                Token.INT_CONSTANT(1), Token.INT_CONSTANT(4), Token.EOF
        };

        doTokenizerTest(text, correct);
    }

    @Test
    public void commentTest2() {
        String text = "1 {2 {3} 4} 5";

        Token[] correct = new Token[] {
                Token.INT_CONSTANT(1), Token.INT_CONSTANT(5), Token.EOF
        };

        doTokenizerTest(text, correct);
    }

    @Test
    public void badCommentTest1() {
        String text = "1+-/{";

        Token[] correct = new Token[] {
                Token.INT_CONSTANT(1), Token.PLUS, Token.MINUS, Token.REAL_DIVIDE
        };

        doTokenizerTestThenError(text, correct,
                "EOF reached, still 1 comment levels deep");
    }

    @Test
    public void badCommentTest2() {
        String text = "1+{-1{2}1;}12{{}{{}";

        Token[] correct = new Token[] {
                Token.INT_CONSTANT(1), Token.PLUS, Token.INT_CONSTANT(12)
        };

        doTokenizerTestThenError(text, correct,
                "EOF reached, still 2 comment levels deep");
    }
}
