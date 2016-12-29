package io.github.rodyamirov.calc.tree;

import io.github.rodyamirov.calc.Token;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/22/16.
 */
public class SyntaxTreeTest {
    private SyntaxTree<Integer> example1; // (-12)
    private SyntaxTree<Integer> example2; // 1 * (-13)
    private SyntaxTree<Integer> example3; // 1+((13*-1)/(-1--3))
    private SyntaxTree<Integer> example4; // +-(1-13)

    @Before
    public void setup() {
        example1 = makeExample1();
        example2 = makeExample2();
        example3 = makeExample3();
        example4 = makeExample4();
    }

    private SyntaxTree<Integer> makeExample4() {
        return new UnaryOpNode(
                new UnaryOpNode(
                        new BinOpNode(
                                new ConstantNode(1),
                                new ConstantNode(13),
                                Token.MINUS
                        ),
                        Token.MINUS
                ),
                Token.PLUS
        );
    }

    private SyntaxTree<Integer> makeExample3() {
        return new BinOpNode(
                new ConstantNode(1),
                new BinOpNode(
                        new BinOpNode(
                                new ConstantNode(13),
                                new ConstantNode(-1),
                                Token.TIMES
                        ),
                        new BinOpNode(
                                new ConstantNode(-1),
                                new ConstantNode(-3),
                                Token.MINUS
                        ),
                        Token.DIVIDE
                ),
                Token.PLUS
        );
    }

    private SyntaxTree<Integer> makeExample2() {
        return new BinOpNode(
                new ConstantNode(1),
                new ConstantNode(-13),
                Token.TIMES
        );
    }

    private SyntaxTree<Integer> makeExample1() {
        return new ConstantNode(-12);
    }

    @Test
    public void evaluationTest() {
        assertThat(example1.evaluate(), is(-12));
        assertThat(example2.evaluate(), is(-13));
        assertThat(example3.evaluate(), is(-5));
        assertThat(example4.evaluate(), is(12));
    }

    @Test
    public void reversePolishNotationTest() {
        assertThat(example1.reversePolishNotation(), is("-12"));
        assertThat(example2.reversePolishNotation(), is("1 -13 TIMES"));
        assertThat(example3.reversePolishNotation(), is("1 13 -1 TIMES -1 -3 MINUS DIVIDE PLUS"));
        assertThat(example4.reversePolishNotation(), is("1 13 MINUS MINUS PLUS"));
    }

    @Test
    public void lispNotationTest() {
        assertThat(example1.lispNotation(), is("-12"));
        assertThat(example2.lispNotation(), is("(TIMES 1 -13)"));
        assertThat(example3.lispNotation(), is("(PLUS 1 (DIVIDE (TIMES 13 -1) (MINUS -1 -3)))"));
        assertThat(example4.lispNotation(), is("(PLUS (MINUS (MINUS 1 13)))"));
    }
}
