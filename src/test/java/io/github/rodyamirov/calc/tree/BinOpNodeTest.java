package io.github.rodyamirov.calc.tree;

import io.github.rodyamirov.calc.Token;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/22/16.
 */
public class BinOpNodeTest {
    @Test
    public void constructorValidationTest() {
        Token[] opTokens = new Token[] { Token.PLUS, Token.TIMES, Token.DIVIDE, Token.MINUS };
        ConstantNode x = new ConstantNode(12);

        for (Token token : opTokens) {
            try {
                new BinOpNode(null, x, token);
                assertThat("Should have thrown an error", true, is(false));
            } catch (IllegalArgumentException iae) {
                // good
            }

            try {
                new BinOpNode(x, null, token);
                assertThat("Should have thrown an error", true, is(false));
            } catch (IllegalArgumentException iae) {
                // good
            }

            try {
                new BinOpNode(x, x, null);
                assertThat("Should have thrown an error", true, is(false));
            } catch (IllegalArgumentException iae) {
                // good
            }
        }

        try {
            new BinOpNode(x, x, Token.EOF);
            assertThat("Should have thrown an error", true, is(false));
        } catch (IllegalArgumentException iae) {
            // good
        }
    }
}
