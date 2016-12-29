package io.github.rodyamirov.calc;

import io.github.rodyamirov.calc.tree.SyntaxTree;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/22/16.
 */
public class ParserTest {
    private void checkParseAnswer(String toParse, int result) {
        SyntaxTree tree = new Parser(toParse).parse();
        assertThat(tree.evaluate(), is(result));
    }

    private void checkParseLisp(String toParse, String lisp) {
        SyntaxTree tree = new Parser(toParse).parse();
        assertThat(tree.lispNotation(), is(lisp));
    }

    private void checkParseRPS(String toParse, String rps) {
        SyntaxTree tree = new Parser(toParse).parse();
        assertThat(tree.reversePolishNotation(), is(rps));
    }

    @Test
    public void complexTest() {
        checkParseAnswer("1+2-3+4", 4);
        checkParseAnswer("   12", 12);
        checkParseAnswer("1-\t 3*4   ", -11);
        checkParseAnswer("1/2", 0);
        checkParseAnswer("1*12/3-4+2", 2);
        checkParseAnswer("\t\t\t\t\n\r1\t\n ", 1);

        checkParseAnswer("1+(2-3)/(3-(4+1))", 1);
        checkParseAnswer("(1+(4*2+(( 2+\t1)\n -    2)*7)/2-1)*2", 14);

        checkParseAnswer("-12", -12);
        checkParseAnswer("1+-2", -1);
        checkParseAnswer("(1+-+2)--(1*12)", 11);
        checkParseLisp("(1+-+2)--(1*12)", "(MINUS (PLUS 1 (MINUS (PLUS 2))) (MINUS (TIMES 1 12)))");
        checkParseRPS("1+2+3+4", "1 2 PLUS 3 PLUS 4 PLUS");
    }

    @Test
    public void basicParseTest() {
        for (int i = -10; i < 30; i++) {
            checkParseAnswer(""+i, i);
        }
    }

    @Test
    public void basicOpTest() {
        for (int i = -10; i < 30; i++) {
            for (int j = -10; j < 30; j++) {
                checkParseAnswer(i + "+" + j, i + j);
                checkParseAnswer(i + "-" + j, i - j);
                checkParseAnswer(i + "*" + j, i * j);
                if (j != 0) {
                    checkParseAnswer(i + "/" + j, i / j);
                }
            }
        }
    }

    @Test
    public void basicPrecedenceTest() {
        for (int i = -10; i < 10; i++) {
            for (int j = -10; j < 10; j++) {
                for (int k = -10; k < 10; k++) {
                    // of course depending on java doing precedence correctly, which it does
                    checkParseAnswer(i + "+" + j + "+" + k, i+j+k);
                    checkParseAnswer(i + "+" + j + "-" + k, i+j-k);
                    checkParseAnswer(i + "+" + j + "*" + k, i+j*k);
                    if (k != 0) {
                        checkParseAnswer(i + "+" + j + "/" + k, i + j / k);
                    }

                    checkParseAnswer(i + "-" + j + "+" + k, i-j+k);
                    checkParseAnswer(i + "-" + j + "-" + k, i-j-k);
                    checkParseAnswer(i + "-" + j + "*" + k, i-j*k);
                    if (k != 0) {
                        checkParseAnswer(i + "-" + j + "/" + k, i - j / k);
                    }

                    checkParseAnswer(i + "*" + j + "+" + k, i*j+k);
                    checkParseAnswer(i + "*" + j + "-" + k, i*j-k);
                    checkParseAnswer(i + "*" + j + "*" + k, i*j*k);
                    if (k != 0) {
                        checkParseAnswer(i + "*" + j + "/" + k, i * j / k);
                    }

                    if (j != 0) {
                        checkParseAnswer(i + "/" + j + "+" + k, i / j + k);
                        checkParseAnswer(i + "/" + j + "-" + k, i / j - k);
                        checkParseAnswer(i + "/" + j + "*" + k, i / j * k);
                        if (k != 0) {
                            checkParseAnswer(i + "/" + j + "/" + k, i / j / k);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void basicParenTest() {
        for (int i = -10; i < 10; i++) {
            for (int j = -10; j < 10; j++) {
                for (int k = -10; k < 10; k++) {
                    // of course depending on java doing precedence correctly, which it does
                    checkParseAnswer("(" + i + "+" + j + ")+" + k, (i+j)+k);
                    checkParseAnswer("(" + i + "+" + j + ")-" + k, (i+j)-k);
                    checkParseAnswer("(" + i + "+" + j + ")*" + k, (i+j)*k);
                    if (k != 0) {
                        checkParseAnswer("(" + i + "+" + j + ")/" + k, (i+j) / k);
                    }

                    checkParseAnswer("(" + i + "-" + j + ")+" + k, (i-j)+k);
                    checkParseAnswer("(" + i + "-" + j + ")-" + k, (i-j)-k);
                    checkParseAnswer("(" + i + "-" + j + ")*" + k, (i-j)*k);
                    if (k != 0) {
                        checkParseAnswer("(" + i + "-" + j + ")/" + k, (i-j) / k);
                    }

                    checkParseAnswer("(" + i + "*" + j + ")+" + k, (i*j)+k);
                    checkParseAnswer("(" + i + "*" + j + ")-" + k, (i*j)-k);
                    checkParseAnswer("(" + i + "*" + j + ")*" + k, (i*j)*k);
                    if (k != 0) {
                        checkParseAnswer("(" + i + "*" + j + ")/" + k, (i*j)/ k);
                    }

                    if (j != 0) {
                        checkParseAnswer("(" + i + "/" + j + ")+" + k, (i/j) + k);
                        checkParseAnswer("(" + i + "/" + j + ")-" + k, (i/j) - k);
                        checkParseAnswer("(" + i + "/" + j + ")*" + k, (i/j) * k);
                        if (k != 0) {
                            checkParseAnswer("(" + i + "/" + j + ")/" + k, (i/j) / k);
                        }
                    }

                    // of course depending on java doing precedence correctly, which it does
                    checkParseAnswer(i + "+(" + j + "+" + k + ")", i+(j+k));
                    checkParseAnswer(i + "+(" + j + "-" + k + ")", i+(j-k));
                    checkParseAnswer(i + "+(" + j + "*" + k + ")", i+(j*k));
                    if (k != 0) {
                        checkParseAnswer(i + "+(" + j + "/" + k + ")", i + (j/k));
                    }

                    checkParseAnswer(i + "-(" + j + "+" + k + ")", i-(j+k));
                    checkParseAnswer(i + "-(" + j + "-" + k + ")", i-(j-k));
                    checkParseAnswer(i + "-(" + j + "*" + k + ")", i-(j*k));
                    if (k != 0) {
                        checkParseAnswer(i + "-(" + j + "/" + k + ")", i - (j/k));
                    }

                    checkParseAnswer(i + "*(" + j + "+" + k + ")", i*(j+k));
                    checkParseAnswer(i + "*(" + j + "-" + k + ")", i*(j-k));
                    checkParseAnswer(i + "*(" + j + "*" + k + ")", i*(j*k));
                    if (k != 0) {
                        checkParseAnswer(i + "*(" + j + "/" + k + ")", i * (j/k));
                    }

                    if (j + k != 0) {
                        checkParseAnswer(i + "/(" + j + "+" + k + ")", i / (j + k));
                    }

                    if (j-k != 0) {
                        checkParseAnswer(i + "/(" + j + "-" + k + ")", i / (j - k));
                    }

                    if (j*k != 0) {
                        checkParseAnswer(i + "/(" + j + "*" + k + ")", i / (j * k));
                    }

                    if (k != 0 && j/k != 0) {
                        checkParseAnswer(i + "/(" + j + "/" + k + ")", i / (j/k));
                    }
                }
            }
        }
    }

    @Test
    public void basicUnaryTest() {
        checkParseAnswer("----1", 1);
        checkParseAnswer("--+-1", -1);
        checkParseAnswer("+ -+ -1", 1);
        checkParseAnswer("-++-1", 1);

        checkParseLisp("-++ + -12", "(MINUS (PLUS (PLUS (PLUS (MINUS 12)))))");
    }
}
