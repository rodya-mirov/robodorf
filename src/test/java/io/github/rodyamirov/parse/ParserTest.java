package io.github.rodyamirov.parse;

import com.google.common.collect.ImmutableList;
import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.TypeSpec;
import io.github.rodyamirov.tree.AndThenNode;
import io.github.rodyamirov.tree.AssignNode;
import io.github.rodyamirov.tree.BinOpNode;
import io.github.rodyamirov.tree.BlockNode;
import io.github.rodyamirov.tree.BooleanConstantNode;
import io.github.rodyamirov.tree.CompoundNode;
import io.github.rodyamirov.tree.DeclarationNode;
import io.github.rodyamirov.tree.ExpressionNode;
import io.github.rodyamirov.tree.IfStatementNode;
import io.github.rodyamirov.tree.IntConstantNode;
import io.github.rodyamirov.tree.NoOpNode;
import io.github.rodyamirov.tree.OrElseNode;
import io.github.rodyamirov.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.tree.ProgramNode;
import io.github.rodyamirov.tree.RealConstantNode;
import io.github.rodyamirov.tree.SyntaxTree;
import io.github.rodyamirov.tree.UnaryOpNode;
import io.github.rodyamirov.tree.VariableAssignNode;
import io.github.rodyamirov.tree.VariableDeclarationNode;
import io.github.rodyamirov.tree.VariableEvalNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/25/16.
 */
public class ParserTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private void doParseExpressionTest(String[] texts, SyntaxTree desired) {
        for (String text : texts) {
            Parser parser = new Parser(text);
            SyntaxTree actual = parser.parseExpression();

            assertThat("Got the correct expression tree", actual, is(desired));
        }
    }

    private void doParseProgramTest(String[] texts, SyntaxTree desired) {
        for (String text : texts) {
            Parser parser = new Parser(text);
            SyntaxTree actual = parser.parseProgram();
            assertThat("Got the correct parse tree", actual, is(desired));
        }
    }

    private UnaryOpNode minus(ExpressionNode node) {
        return new UnaryOpNode(node, Token.MINUS);
    }

    @Test
    public void exprTest1() {
        SyntaxTree parsed = minus(IntConstantNode.make(12));
        String[] toParse = new String[] {
                "-12",
                "-(12)",
                "(-12)",
                "((-((   12)) )\t)"
        };
        doParseExpressionTest(toParse, parsed);
    }

    @Test
    public void exprTest2() {
        SyntaxTree exprTree2 = new BinOpNode(
                RealConstantNode.make(1.1f),
                minus(IntConstantNode.make(13)),
                Token.TIMES
        );

        String[] exprText2 = new String[] {
                "1.1 * (-13)",
                "(1.1) * -(13)",
                "1.1*-13"
        };

        doParseExpressionTest(exprText2, exprTree2);
    }

    @Test
    public void exprTest3() {
        SyntaxTree exprTree3 = new BinOpNode(
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

        String[] exprText3 = new String[] {
                "1+((13*(-1))div(-1.12--3))",
                "1+(13*-1 div(-1.12--3))"
        };

        doParseExpressionTest(exprText3, exprTree3);
    }

    @Test
    public void exprTest4() {
        SyntaxTree exprTree4 = new UnaryOpNode(
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

        String[] exprText4 = new String[] {
                "+-(1-13.7)",
                "+(-(1-13.7))",
                "((+ (-((1)-(13.7)))))"
        };

        doParseExpressionTest(exprText4, exprTree4);
    }

    @Test
    public void exprTest5() {
        SyntaxTree exprTree5 = RealConstantNode.make(1.1f);

        String[] exprText5 = new String[] {
                "1.1",
                "(((1.1)))"
        };

        doParseExpressionTest(exprText5, exprTree5);
    }

    @Test
    public void exprTest6() {
        SyntaxTree exprTree6 = new BinOpNode(
                new BinOpNode(
                        IntConstantNode.make(1),
                        IntConstantNode.make(2),
                        Token.MOD
                ),
                IntConstantNode.make(3),
                Token.MOD
        );

        String[] exprText6 = new String[] {
                "1 MoD 2 mod 3",
                "(1 MOd 2) MOD 3"
        };

        doParseExpressionTest(exprText6, exprTree6);
    }

    @Test
    public void exprTest7() {
        SyntaxTree tree = new BinOpNode(
                new BinOpNode(
                        new VariableEvalNode(Token.ID("a")),
                        new VariableEvalNode(Token.ID("b")),
                        Token.MOD
                ),
                IntConstantNode.make(12),
                Token.PLUS
        );

        String[] text = new String[] {
                "a mod b+12",
                "(a MOD b) + 12"
        };

        doParseExpressionTest(text, tree);
    }

    @Test
    public void exprTest8() {
        // it just has to parse, not evaluate reasonably
        SyntaxTree tree = new BinOpNode(
                BooleanConstantNode.make(true),
                BooleanConstantNode.make(false),
                Token.PLUS
        );

        String[] text = new String[] {
                "true + false",
                "true+false",
                "(true+(false))"
        };

        doParseExpressionTest(text, tree);
    }

    @Test
    public void shortCircuitTest1() {
        SyntaxTree tree = new AndThenNode(
                BooleanConstantNode.make(true),
                BooleanConstantNode.make(false)
        );

        String[] text = new String[] {
                "true and Then false",
                "(true) And then (false)",
                "true and thEN (false)",
                "(true) AND tHen false",
                "(true and then false)"
        };

        doParseExpressionTest(text, tree);
    }

    @Test
    public void shortCircuitTest2() {
        SyntaxTree tree = new OrElseNode(
                BooleanConstantNode.make(true),
                BooleanConstantNode.make(false)
        );

        String[] text = new String[] {
                "true or else false",
                "(true) or else (false)",
                "true OR ELSE (false)",
                "(true) Or Else false",
                "(true oR eLsE false)"
        };

        doParseExpressionTest(text, tree);
    }

    @Test
    public void boolExprTest1() {
        // tests NOT/OR/AND
        SyntaxTree tree = new BinOpNode(
                BooleanConstantNode.make(true),
                new BinOpNode(
                        new UnaryOpNode(
                                new VariableEvalNode(Token.ID("someBool")),
                                Token.NOT
                        ),
                        BooleanConstantNode.make(false),
                        Token.OR
                ),
                Token.AND
        );

        String[] text = new String[] {
                "true and (not somebool or false)",
                "true and ((not somebool) or false)",
        };

        doParseExpressionTest(text, tree);

        // same, but checks operator precedence
        tree = new BinOpNode(
                new BinOpNode(
                        new UnaryOpNode(
                                new VariableEvalNode(Token.ID("someBool")),
                                Token.NOT
                        ),
                        BooleanConstantNode.make(false),
                        Token.OR
                ),
                BooleanConstantNode.make(true),
                Token.AND
        );

        text = new String[] {
                "(not somebool or false) and true",
                "((not somebool) or false) and true"
        };

        doParseExpressionTest(text, tree);

        // same, but checks operator precedence
        tree = new BinOpNode(
                new BinOpNode(
                        new UnaryOpNode(
                                new VariableEvalNode(Token.ID("someBool")),
                                Token.NOT
                        ),
                        BooleanConstantNode.make(false),
                        Token.AND
                ),
                BooleanConstantNode.make(true),
                Token.OR
        );

        text = new String[] {
                "(not somebool and false) or true",
                "((not somebool) and false) or true",
                "(not somebool) and false or true",
                "not somebool and false or true"
        };

        doParseExpressionTest(text, tree);

        // same, but checks operator precedence
        tree = new BinOpNode(
                BooleanConstantNode.make(true),
                new BinOpNode(
                        new UnaryOpNode(
                                new VariableEvalNode(Token.ID("someBool")),
                                Token.NOT
                        ),
                        BooleanConstantNode.make(false),
                        Token.AND
                ),
                Token.OR
        );

        text = new String[] {
                "true or not somebool and false",
                "true or (not somebool and false)",
                "true or ((not somebool) and false)",
                "true or (not somebool) and false"
        };

        doParseExpressionTest(text, tree);
    }

    @Test
    public void boolExprTest2() {
        String[] text;
        SyntaxTree tree;

        text = new String[] {
                "a or else b and c",
                "a or else (b and c)",
        };
        tree = new OrElseNode(
                new VariableEvalNode(Token.ID("a")),
                new BinOpNode(
                        new VariableEvalNode(Token.ID("b")),
                        new VariableEvalNode(Token.ID("c")),
                        Token.AND
                )
        );
        doParseExpressionTest(text, tree);

        text = new String[] {
                "a or else b or c",
                "a or else (b or c)"
        };
        tree = new OrElseNode(
                new VariableEvalNode(Token.ID("a")),
                new BinOpNode(
                        new VariableEvalNode(Token.ID("b")),
                        new VariableEvalNode(Token.ID("c")),
                        Token.OR
                )
        );
        doParseExpressionTest(text, tree);

        text = new String[] {
                "a and then b or c",
                "a and then (b or c)"
        };
        tree = new AndThenNode(
                new VariableEvalNode(Token.ID("a")),
                new BinOpNode(
                        new VariableEvalNode(Token.ID("b")),
                        new VariableEvalNode(Token.ID("c")),
                        Token.OR
                )
        );
        doParseExpressionTest(text, tree);

        text = new String[] {
                "a and then b and c",
                "a and then (b and c)"
        };
        tree = new AndThenNode(
                new VariableEvalNode(Token.ID("a")),
                new BinOpNode(
                        new VariableEvalNode(Token.ID("b")),
                        new VariableEvalNode(Token.ID("c")),
                        Token.AND
                )
        );
        doParseExpressionTest(text, tree);
    }

    @Test
    public void compareExprTest1() {
        // just check that the parser recognizes all six comparison operators...
        String[] text;
        SyntaxTree tree;

        text = new String[] {
                "a < b", "(a)<b", "(a<b)", "a<(b)"
        };
        tree = new BinOpNode(
                new VariableEvalNode(Token.ID("a")),
                new VariableEvalNode(Token.ID("b")),
                Token.LESS_THAN
        );
        doParseExpressionTest(text, tree);

        text = new String[] {
                "a <= b", "(a)<=b", "(a<=b)", "a<=(b)"
        };
        tree = new BinOpNode(
                new VariableEvalNode(Token.ID("a")),
                new VariableEvalNode(Token.ID("b")),
                Token.LESS_THAN_OR_EQUALS
        );
        doParseExpressionTest(text, tree);

        text = new String[] {
                "a <> b", "(a)<>b", "(a<>b)", "a<>(b)"
        };
        tree = new BinOpNode(
                new VariableEvalNode(Token.ID("a")),
                new VariableEvalNode(Token.ID("b")),
                Token.NOT_EQUALS
        );
        doParseExpressionTest(text, tree);

        text = new String[] {
                "a > b", "(a)>b", "(a>b)", "a>(b)"
        };
        tree = new BinOpNode(
                new VariableEvalNode(Token.ID("a")),
                new VariableEvalNode(Token.ID("b")),
                Token.GREATER_THAN
        );
        doParseExpressionTest(text, tree);

        text = new String[] {
                "a >= b", "(a)>=b", "(a>=b)", "a>=(b)"
        };
        tree = new BinOpNode(
                new VariableEvalNode(Token.ID("a")),
                new VariableEvalNode(Token.ID("b")),
                Token.GREATER_THAN_OR_EQUALS
        );
        doParseExpressionTest(text, tree);

        text = new String[] {
                "a = b", "(a)=b", "(a=b)", "a=(b)"
        };
        tree = new BinOpNode(
                new VariableEvalNode(Token.ID("a")),
                new VariableEvalNode(Token.ID("b")),
                Token.EQUALS
        );
        doParseExpressionTest(text, tree);
    }

    @Test
    public void booleanPrecedenceTest1() {
        // check operator precedence; not exhaustive because ugh
        String[] text;
        SyntaxTree tree;

        text = new String[] {
                "not a + b and c",
                "(not a) + b and c",
                "(not a) + (b and c)"
        };

        tree = new BinOpNode(
                new UnaryOpNode(new VariableEvalNode(Token.ID("a")), Token.NOT),
                new BinOpNode(
                        new VariableEvalNode(Token.ID("b")),
                        new VariableEvalNode(Token.ID("c")),
                        Token.AND
                ),
                Token.PLUS
        );

        doParseExpressionTest(text, tree);
    }

    @Test
    public void booleanPrecedenceTest2() {
        String[] text;
        SyntaxTree tree;

        text = new String[] {
                "a or b and c or else d",
                "a or (b and c) or else d",
                "(a or (b and c)) or else d",
                "(a or b and c) or else d"
        };
        tree = new OrElseNode(
                new BinOpNode(
                        new VariableEvalNode(Token.ID("a")),
                        new BinOpNode(
                                new VariableEvalNode(Token.ID("b")),
                                new VariableEvalNode(Token.ID("c")),
                                Token.AND
                        ),
                        Token.OR
                ),
                new VariableEvalNode(Token.ID("d"))
        );
        doParseExpressionTest(text, tree);

        text = new String[] {
                "a and b or c or else d",
                "(a and b) or c or else d",
                "((a and b) or c) or else d",
                "(a and b or c) or else d"
        };
        tree = new OrElseNode(
                new BinOpNode(
                        new BinOpNode(
                                new VariableEvalNode(Token.ID("a")),
                                new VariableEvalNode(Token.ID("b")),
                                Token.AND
                        ),
                        new VariableEvalNode(Token.ID("c")),
                        Token.OR
                ),
                new VariableEvalNode(Token.ID("d"))
        );
        doParseExpressionTest(text, tree);
    }

    @Test
    public void ifStatementTest1() {
        ProgramNode programNode = new ProgramNode(Token.ID("testProg"), new BlockNode(
                new DeclarationNode(list(), list()), // no declarations ...
                new CompoundNode(list(
                        new IfStatementNode(
                                Parser.parseExpression("1<2"),
                                new NoOpNode(),
                                new IfStatementNode(
                                        Parser.parseExpression("2<3"),
                                        new NoOpNode(),
                                        new IfStatementNode(
                                                Parser.parseExpression("3<4"),
                                                new NoOpNode(),
                                                new NoOpNode()
                                        )
                                )
                        )
                ))
        ));

        String[] text = new String[] {
                "program testProg {dnskdnsk};"
                        + "begin "
                        + "  if (1 < 2) then"
                        + "     {nothing}"
                        + "  else if (2 < 3) then"
                        + "     {nothing!}"
                        + "  else if (3 < 4) then"
                        + "     {nothing!}"
                        + "  else"
                        + "     {nothing!}"
                        + "end ."
        };

        doParseProgramTest(text, programNode);
    }

    @Test
    public void ifStatementTest2() {
        ProgramNode programNode = new ProgramNode(Token.ID("testProg"), new BlockNode(
                new DeclarationNode(list(), list()), // no declarations ...
                new CompoundNode(list(
                        new IfStatementNode(
                                Parser.parseExpression("1<2"),
                                new NoOpNode(),
                                new IfStatementNode(
                                        Parser.parseExpression("2<3"),
                                        new NoOpNode(),
                                        new IfStatementNode(
                                                Parser.parseExpression("3<4"),
                                                new NoOpNode()
                                        )
                                )
                        )
                ))
        ));

        String[] text = new String[] {
                "program testProg {dnskdnsk};"
                        + "begin "
                        + "  if (1 < 2) then"
                        + "     {nothing}"
                        + "  else if (2 < 3) then"
                        + "     {nothing!}"
                        + "  else if (3 < 4) then"
                        + "     {nothing!}"
                        + "end ."
        };

        doParseProgramTest(text, programNode);
    }

    @Test
    public void ifStatementTest3() {
        ProgramNode programNode = new ProgramNode(Token.ID("testProg"), new BlockNode(
                new DeclarationNode(list(), list()), // no declarations ...
                new CompoundNode(list(
                        new IfStatementNode(
                                Parser.parseExpression("1<2"),
                                new IfStatementNode(
                                        Parser.parseExpression("2<3"),
                                        new NoOpNode(),
                                        new NoOpNode()
                                ),
                                new NoOpNode()
                        )
                ))
        ));

        String[] text = new String[] {
                "program testProg {dnskdnsk};"
                        + "begin "
                        + "  if (1 < 2) then"
                        + "     if (2 < 3) then"
                        + "         {nothing!}"
                        + "     else "
                        + "         {nothing!}"
                        + "  else"
                        + "     {nothing!}"
                        + "end ."
        };

        doParseProgramTest(text, programNode);
    }

    @Test
    public void ifStatementTest4() {
        ProgramNode programNode = null; // parsing is gonna fail anyway

        String[] text = new String[] {
                "program testProg {dnskdnsk};"
                        + "begin "
                        + "  if (1 < 2) then"
                        + "     if (2 < 3) then"
                        + "         {nothing!}"
                        + "     else "
                        + "         {nothing!};" // <-- this fucks everything because Pascal hates you
                        + "  else"
                        + "     {nothing!}"
                        + "end ."
        };

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(is(String.format("Cannot accept type %s", Token.Type.ELSE.name())));

        doParseProgramTest(text, programNode);

        // reset `thrown`
        thrown = ExpectedException.none();
    }

    @Test
    public void comparatorPrecedenceTest1() {
        // check operator precedence; not exhaustive because ugh
        String[] text;
        SyntaxTree tree;

        text = new String[] {
                "1>3+2>3*4",
                "1>3+2>(3*4)",
                "1>(3+2)>(3*4)",
                "(1>(3+2))>(3*4)",
                "(1>3+2)>(3*4)"
        };

        tree = new BinOpNode(
                new BinOpNode(
                        IntConstantNode.make(1),
                        new BinOpNode(
                                IntConstantNode.make(3),
                                IntConstantNode.make(2),
                                Token.PLUS
                        ),
                        Token.GREATER_THAN
                ),
                new BinOpNode(
                        IntConstantNode.make(3),
                        IntConstantNode.make(4),
                        Token.TIMES
                ),
                Token.GREATER_THAN
        );

        doParseExpressionTest(text, tree);
    }

    private List<Token<String>> varList(String... ids) {
        List<Token<String>> out = new ArrayList<>();

        for (String id : ids) {
            out.add(Token.ID(id));
        }

        return out;
    }

    private <T> List<T> list(T... elements) {
        List<T> out = new ArrayList<>();

        for (T element : elements) {
            out.add(element);
        }

        return out;
    }

    @Test
    public void progTest1() {
        // standard empty program
        SyntaxTree progTree1 = new ProgramNode(
                Token.ID("test1"),
                new BlockNode(
                        new DeclarationNode(Collections.emptyList(), Collections.emptyList()),
                        new CompoundNode(list(new NoOpNode()))
                )
        );
        String[] progText1 = new String[] {
                "Program test1; begin end.",
                "pROgRam Test1; begin end."
        };
        doParseProgramTest(progText1, progTree1);
    }

    @Test
    public void progTest2() {
        // declare some variables, do nothing
        SyntaxTree progTree2 = new ProgramNode(
                Token.ID("test2"),
                new BlockNode(
                        new DeclarationNode(
                                list(
                                        new VariableDeclarationNode(
                                                ImmutableList.of(Token.ID("number"),
                                                        Token.ID("other_number")),
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
        String[] progText2 = new String[] {
                "program test2;"
                        + "var number, other_number: integer; ril, _r: real; a: integer; "
                        + "begin end"
                        + "."
        };
        doParseProgramTest(progText2, progTree2);
    }

    @Test
    public void progTest3() {
        SyntaxTree progTree3 = new ProgramNode(Token.ID("test3"), new BlockNode(
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
        String[] progText3 = new String[] {
                "program Test3; var a: Integer; b {real i guess?}: REAL;"
                        + "BEGIN {Test3}"
                        + "a := 12*(-12)+4;"
                        + "b := 1-12*a;"
                        + "a := 1;"
                        + "end."
        };
        doParseProgramTest(progText3, progTree3);
    }

    @Test
    public void progTest4() {
        SyntaxTree progTree4 = new ProgramNode(
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
        String[] progText4 = new String[] {
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
        doParseProgramTest(progText4, progTree4);
    }

    @Test
    public void progTest5() {
        SyntaxTree progTree5 = new ProgramNode(Token.ID("test5"), new BlockNode(
                new DeclarationNode(
                        list(
                                new VariableDeclarationNode(varList("a", "b"), TypeSpec.BOOLEAN),
                                new VariableDeclarationNode(varList("c"), TypeSpec.BOOLEAN),
                                new VariableDeclarationNode(varList("d"), TypeSpec.REAL)
                        ), list() // no procedures here
                ),
                new CompoundNode(list(
                        new AssignNode(new VariableAssignNode(Token.ID("a")), BooleanConstantNode.make(true)),
                        new AssignNode(new VariableAssignNode(Token.ID("b")), BooleanConstantNode.make(false)),
                        new AssignNode(
                                new VariableAssignNode(Token.ID("c")),
                                new BinOpNode(
                                        new VariableEvalNode(Token.ID("a")),
                                        new VariableEvalNode(Token.ID("b")),
                                        Token.PLUS
                                )
                        ),
                        new AssignNode(new VariableAssignNode(Token.ID("d")), new VariableEvalNode(Token.ID("yes")))
                ))
        ));

        // note no semantic analysis during the parse phase, so type checking (c and d) and
        // variable existence (d) are not relevant
        String[] progText5 = new String[] {
                "program test5;"
                        + "var a, b: Boolean; c: Boolean; d: REAL;"
                        + "begin {test5 starts here!}"
                        + "  a := true; b := false; c := a+b; d := yes"
                        + "end {all over}"
        };
    }
}