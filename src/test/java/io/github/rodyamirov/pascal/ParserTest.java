package io.github.rodyamirov.pascal;

import com.google.common.collect.ImmutableList;
import io.github.rodyamirov.pascal.tree.AssignNode;
import io.github.rodyamirov.pascal.tree.BinOpNode;
import io.github.rodyamirov.pascal.tree.BlockNode;
import io.github.rodyamirov.pascal.tree.CompoundNode;
import io.github.rodyamirov.pascal.tree.DeclarationNode;
import io.github.rodyamirov.pascal.tree.ExpressionNode;
import io.github.rodyamirov.pascal.tree.IntConstantNode;
import io.github.rodyamirov.pascal.tree.NoOpNode;
import io.github.rodyamirov.pascal.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.pascal.tree.ProgramNode;
import io.github.rodyamirov.pascal.tree.RealConstantNode;
import io.github.rodyamirov.pascal.tree.SyntaxTree;
import io.github.rodyamirov.pascal.tree.UnaryOpNode;
import io.github.rodyamirov.pascal.tree.VariableAssignNode;
import io.github.rodyamirov.pascal.tree.VariableDeclarationNode;
import io.github.rodyamirov.pascal.tree.VariableEvalNode;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/25/16.
 */
public class ParserTest {
    // the tree is the result of parsing; the strings are various strings which parse to that tree
    private SyntaxTree exprTree1, exprTree2, exprTree3, exprTree4, exprTree5, exprTree6;
    private String[] exprText1, exprText2, exprText3, exprText4, exprText5, exprText6;

    private SyntaxTree progTree1, progTree2, progTree3, progTree4;
    private String[] progText1, progText2, progText3, progText4;

    @Before
    public void setup() {
        setupExprExamples();
        setupProgExamples();
    }

    private UnaryOpNode minus(ExpressionNode node) {
        return new UnaryOpNode(node, Token.MINUS);
    }

    private void setupExprExamples() {
        exprTree1 = minus(IntConstantNode.make(12));
        exprText1 = new String[] {
                "-12",
                "-(12)",
                "(-12)",
                "((-((   12)) )\t)"
        };

        exprTree2 = new BinOpNode(
                RealConstantNode.make(1.1f),
                minus(IntConstantNode.make(13)),
                Token.TIMES
        );
        exprText2 = new String[] {
                "1.1 * (-13)",
                "(1.1) * -(13)",
                "1.1*-13"
        };

        exprTree3 = new BinOpNode(
                IntConstantNode.make(1),
                new BinOpNode(
                        new BinOpNode(
                                IntConstantNode.make(13),
                                minus(IntConstantNode.make(1)),
                                Token.TIMES
                        ),
                        new BinOpNode(
                                minus(RealConstantNode.make(1.12f)),
                                minus(IntConstantNode.make(3)),
                                Token.MINUS
                        ),
                        Token.INT_DIVIDE
                ),
                Token.PLUS
        );
        exprText3 = new String[] {
                "1+((13*(-1))div(-1.12--3))",
                "1+(13*-1 div(-1.12--3))"
        };

        exprTree4 = new UnaryOpNode(
                new UnaryOpNode(
                        new BinOpNode(
                                IntConstantNode.make(1),
                                RealConstantNode.make(13.7f),
                                Token.MINUS
                        ),
                        Token.MINUS
                ),
                Token.PLUS
        );
        exprText4 = new String[] {
                "+-(1-13.7)",
                "+(-(1-13.7))",
                "((+ (-((1)-(13.7)))))"
        };

        exprTree5 = RealConstantNode.make(1.1f);
        exprText5 = new String[] {
                "1.1",
                "(((1.1)))"
        };

        exprTree6 = new BinOpNode(
                new BinOpNode(
                        IntConstantNode.make(1),
                        IntConstantNode.make(2),
                        Token.MOD
                ),
                IntConstantNode.make(3),
                Token.MOD
        );
        exprText6 = new String[] {
                "1 MoD 2 mod 3",
                "(1 MOd 2) MOD 3"
        };
    }

    private void doParseExpressionTest(String[] texts, SyntaxTree desired) {
        for (String text : texts) {
            Parser parser = new Parser(text);
            SyntaxTree actual = parser.parseExpression();

            assertThat("Got the correct expression tree", actual, is(desired));
        }
    }

    @Test
    public void expressionTest1() {
        doParseExpressionTest(exprText1, exprTree1);
    }

    @Test
    public void expressionTest2() {
        doParseExpressionTest(exprText2, exprTree2);
    }

    @Test
    public void expressionTest3() {
        doParseExpressionTest(exprText3, exprTree3);
    }

    @Test
    public void expressionTest4() {
        doParseExpressionTest(exprText4, exprTree4);
    }

    @Test
    public void expressionTest5() {
        doParseExpressionTest(exprText5, exprTree5);
    }

    @Test
    public void expressionTest6() {
        doParseExpressionTest(exprText6, exprTree6);
    }

    private List<Token<String>> varList(String... ids) {
        List<Token<String>> out = new ArrayList<>();

        for (String id : ids) {
            out.add(Token.ID(id));
        }

        return out;
    }

    private <T> List<T> list(T... elts) {
        List<T> out = new ArrayList<>();

        for (T elt : elts) {
            out.add(elt);
        }

        return out;
    }

    private void setupProgExamples() {
        // standard empty program
        progTree1 = new ProgramNode(
                Token.ID("test1"),
                new BlockNode(
                        new DeclarationNode(Collections.emptyList(), Collections.emptyList()),
                        new CompoundNode(list(new NoOpNode()))
                )
        );
        progText1 = new String[] {
                "Program test1; begin end.",
                "pROgRam Test1; begin end."
        };

        // declare some variables, do nothing
        progTree2 = new ProgramNode(
                Token.ID("test2"),
                new BlockNode(
                        new DeclarationNode(
                                list(
                                        new VariableDeclarationNode(
                                                ImmutableList.of(Token.ID("number"), Token.ID("other_number")),
                                                TypeSpec.INTEGER),
                                        new VariableDeclarationNode(
                                                ImmutableList.of(Token.ID("ril"), Token.ID("_r")),
                                                TypeSpec.REAL),
                                        new VariableDeclarationNode(
                                                ImmutableList.of(Token.ID("a")),
                                                TypeSpec.INTEGER)
                                ),
                                Collections.emptyList() // no procedures
                        ),
                        new CompoundNode(list(new NoOpNode()))
                )
        );
        progText2 = new String[] {
                "program test2;"
                        + "var number, other_number: integer; ril, _r: real; a: integer; "
                        + "begin end"
                        + "."
        };

        progTree3 = new ProgramNode(Token.ID("test3"), new BlockNode(
                new DeclarationNode(
                        list(
                                new VariableDeclarationNode(varList("a"), TypeSpec.INTEGER),
                                new VariableDeclarationNode(varList("b"), TypeSpec.REAL)
                        ),
                        Collections.emptyList() // no procedures
                ),
                new CompoundNode(list(
                        new AssignNode(
                                new VariableAssignNode(Token.ID("a")),
                                new BinOpNode(
                                        new BinOpNode(
                                                IntConstantNode.make(12),
                                                minus(IntConstantNode.make(12)),
                                                Token.TIMES
                                        ),
                                        IntConstantNode.make(4),
                                        Token.PLUS
                                )
                        ),
                        new AssignNode(
                                new VariableAssignNode(Token.ID("b")),
                                new BinOpNode(
                                        IntConstantNode.make(1),
                                        new BinOpNode(
                                                IntConstantNode.make(12),
                                                new VariableEvalNode(Token.ID("a")),
                                                Token.TIMES
                                        ),
                                        Token.MINUS
                                )
                        ),
                        new AssignNode(
                                new VariableAssignNode(Token.ID("a")),
                                IntConstantNode.make(1)
                        ),
                        new NoOpNode()
                ))
        ));
        progText3 = new String[] {
                "program Test3; var a: Integer; b {real i guess?}: REAL;"
                        + "BEGIN {Test3}"
                        + "a := 12*(-12)+4;"
                        + "b := 1-12*a;"
                        + "a := 1;"
                        + "end."
        };

        progTree4 = new ProgramNode(
                Token.ID("test4"),
                new BlockNode(
                        new DeclarationNode(
                                ImmutableList.of(
                                        new VariableDeclarationNode(ImmutableList.of(Token.ID("a")), TypeSpec.INTEGER),
                                        new VariableDeclarationNode(ImmutableList.of(Token.ID("b"), Token.ID("c")), TypeSpec.REAL)
                                ),
                                ImmutableList.of(
                                        new ProcedureDeclarationNode(
                                                Token.ID("proc1"),
                                                new BlockNode(
                                                        new DeclarationNode(
                                                                ImmutableList.of(
                                                                        new VariableDeclarationNode(ImmutableList.of(Token.ID("a")), TypeSpec.REAL),
                                                                        new VariableDeclarationNode(ImmutableList.of(Token.ID("d")), TypeSpec.REAL)
                                                                ),
                                                                ImmutableList.of()
                                                        ),
                                                        new CompoundNode(ImmutableList.of(
                                                                new AssignNode(new VariableAssignNode(Token.ID("a")), IntConstantNode.make(1)),
                                                                new AssignNode(new VariableAssignNode(Token.ID("d")), IntConstantNode.make(4))
                                                        ))
                                                )
                                        )
                                )
                        ),
                        new CompoundNode(ImmutableList.of(
                                new AssignNode(
                                        new VariableAssignNode(Token.ID("a")),
                                        IntConstantNode.make(1)
                                ),
                                new AssignNode(
                                        new VariableAssignNode(Token.ID("b")),
                                        RealConstantNode.make(2.0f)
                                ),
                                new AssignNode(
                                        new VariableAssignNode(Token.ID("c")),
                                        IntConstantNode.make(3)
                                )
                        ))
                )
        );
        progText4 = new String[] {
                "program test4;"
                        + "var a: Integer; b, c: REAl;"
                        + "procedure proc1;"
                        + "  var a: ReAL; d: REAL;"
                        + "  begin {proc1}"
                        + "    a := 1; d := 4"
                        + "  end {proc1};"
                        + "begin {test4}"
                        + "  a := 1; b := 2.0; c := 3"
                        + "end {test4}."
        };
    }

    private void doParseProgramTest(String[] texts, SyntaxTree desired) {
        for (String text : texts) {
            Parser parser = new Parser(text);
            SyntaxTree actual = parser.parseProgram();
            assertThat("Got the correct parse tree", actual, is(desired));
        }
    }

    @Test
    public void programTest1() {
        doParseProgramTest(progText1, progTree1);
    }

    @Test
    public void programTest2() {
        doParseProgramTest(progText2, progTree2);
    }

    @Test
    public void programTest3() {
        doParseProgramTest(progText3, progTree3);
    }

    @Test
    public void programTest4() {
        doParseProgramTest(progText4, progTree4);
    }
}
