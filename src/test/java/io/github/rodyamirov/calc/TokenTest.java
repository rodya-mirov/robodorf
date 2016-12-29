package io.github.rodyamirov.calc;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/22/16.
 */
public class TokenTest {
    @Test
    public void constantTest() {
        assertThat(Token.DIVIDE.type, is(Token.Type.DIVIDE));
        assertThat(Token.DIVIDE.value, is(nullValue()));

        assertThat(Token.TIMES.type, is(Token.Type.TIMES));
        assertThat(Token.TIMES.value, is(nullValue()));

        assertThat(Token.PLUS.type, is(Token.Type.PLUS));
        assertThat(Token.PLUS.value, is(nullValue()));

        assertThat(Token.MINUS.type, is(Token.Type.MINUS));
        assertThat(Token.MINUS.value, is(nullValue()));

        assertThat(Token.EOF.type, is(Token.Type.EOF));
        assertThat(Token.EOF.value, is(nullValue()));

        assertThat(Token.L_PAREN.type, is(Token.Type.L_PAREN));
        assertThat(Token.L_PAREN.value, is(nullValue()));

        assertThat(Token.R_PAREN.type, is(Token.Type.R_PAREN));
        assertThat(Token.R_PAREN.value, is(nullValue()));
    }

    @Test
    public void toStringTest() {
        assertThat(Token.DIVIDE.toString(), is("Token { TYPE: DIVIDE, VALUE: null }"));
        assertThat(Token.TIMES.toString(), is("Token { TYPE: TIMES, VALUE: null }"));

        assertThat(Token.PLUS.toString(), is("Token { TYPE: PLUS, VALUE: null }"));
        assertThat(Token.MINUS.toString(), is("Token { TYPE: MINUS, VALUE: null }"));

        assertThat(Token.L_PAREN.toString(), is("Token { TYPE: L_PAREN, VALUE: null }"));
        assertThat(Token.R_PAREN.toString(), is("Token { TYPE: R_PAREN, VALUE: null }"));

        assertThat(Token.EOF.toString(), is("Token { TYPE: EOF, VALUE: null }"));

        assertThat(Token.INT(12).toString(), is("Token { TYPE: INTEGER, VALUE: 12 }"));
        assertThat(Token.INT(-13).toString(), is("Token { TYPE: INTEGER, VALUE: -13 }"));
        assertThat(Token.INT(1).toString(), is("Token { TYPE: INTEGER, VALUE: 1 }"));
        assertThat(Token.INT(14).toString(), is("Token { TYPE: INTEGER, VALUE: 14 }"));
    }

    @Test
    public void equalsTest() {
        assertThat(Token.INT(13).equals(Token.INT(13)), is(true));
        assertThat(Token.INT(1).equals(Token.INT(1)), is(true));
        assertThat(Token.INT(-135).equals(Token.INT(-135)), is(true));
        assertThat(Token.INT(0).equals(Token.INT(0)), is(true));

        assertThat(Token.INT(13).equals(Token.INT(123)), is(false));
        assertThat(Token.INT(1).equals(Token.INT(11)), is(false));
        assertThat(Token.INT(-135).equals(Token.INT(-1)), is(false));
        assertThat(Token.INT(0).equals(Token.INT(10)), is(false));

        Token[] different = new Token[] {
                Token.DIVIDE, Token.MINUS, Token.INT(12),
                Token.PLUS, Token.EOF, Token.TIMES, Token.INT(15)
        };

        for (int i = 0; i < different.length; i++) {
            for (int j = 0; j < different.length; j++) {
                assertThat(different[i].equals(different[j]), is(i == j));
            }
        }
    }

    @Test
    public void nullCheckTest() {
        try {
            Integer bad = null;
            Token.INT(bad);
            assertThat("Should have thrown an error", false, is(true));
        } catch (NullPointerException npe) {
            // great
        }
    }
}
