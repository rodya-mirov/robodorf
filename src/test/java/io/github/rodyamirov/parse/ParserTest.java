package io.github.rodyamirov.parse;

import com.google.common.collect.ImmutableList;
import io.github.rodyamirov.exceptions.UnexpectedTokenException;
import io.github.rodyamirov.lex.Token;
import io.github.rodyamirov.symbols.TypeSpec;
import io.github.rodyamirov.tree.AndThenNode;
import io.github.rodyamirov.tree.AssignNode;
import io.github.rodyamirov.tree.BinOpNode;
import io.github.rodyamirov.tree.BlockNode;
import io.github.rodyamirov.tree.BooleanConstantNode;
import io.github.rodyamirov.tree.CompoundNode;
import io.github.rodyamirov.tree.DeclarationNode;
import io.github.rodyamirov.tree.DoUntilNode;
import io.github.rodyamirov.tree.ExpressionNode;
import io.github.rodyamirov.tree.ForNode;
import io.github.rodyamirov.tree.IfStatementNode;
import io.github.rodyamirov.tree.IntConstantNode;
import io.github.rodyamirov.tree.LoopControlNode;
import io.github.rodyamirov.tree.NoOpNode;
import io.github.rodyamirov.tree.OrElseNode;
import io.github.rodyamirov.tree.ProcedureCallNode;
import io.github.rodyamirov.tree.ProcedureDeclarationNode;
import io.github.rodyamirov.tree.ProgramNode;
import io.github.rodyamirov.tree.RealConstantNode;
import io.github.rodyamirov.tree.StatementNode;
import io.github.rodyamirov.tree.SyntaxTree;
import io.github.rodyamirov.tree.UnaryOpNode;
import io.github.rodyamirov.tree.VariableAssignNode;
import io.github.rodyamirov.tree.VariableDeclarationNode;
import io.github.rodyamirov.tree.VariableEvalNode;
import io.github.rodyamirov.tree.WhileNode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.rodyamirov.utils.ListHelper.list;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/25/16.
 */
public class ParserTest {
    private static IntConstantNode constant(int value) {
        return IntConstantNode.make(Token.INT_CONSTANT(value));
    }

    private static RealConstantNode constant(float value) {
        return RealConstantNode.make(Token.REAL_CONSTANT(value));
    }

    private static BooleanConstantNode constant(boolean value) {
        return BooleanConstantNode.make(Token.BOOLEAN_CONSTANT(value));
    }

    private UnaryOpNode minus(ExpressionNode node) {
        return new UnaryOpNode(node, Token.MINUS);
    }

    private List<Token<String>> varList(String... ids) {
        List<Token<String>> out = new ArrayList<>();

        for (String id : ids) {
            out.add(Token.ID(id));
        }

        return out;
    }

    private void doParseExpressionTest(String[] texts, SyntaxTree desired) {
        for (String text : texts) {
            ExpressionNode actual = Parser.parseExpression(text);
            assertThat("Got the correct expression tree", actual, is(desired));
        }
    }

    private void doParseStatementTest(String[] texts, StatementNode desired) {
        for (String text : texts) {
            StatementNode actual = Parser.parseStatement(text);
            assertThat("Got the correct statement node", actual, is(desired));
        }
    }

    private void doParseProgramTest(String[] texts, ProgramNode desired) {
        for (String text : texts) {
            ProgramNode actual = Parser.parseProgram(text);
            assertThat("Got the correct parse tree", actual, is(desired));
        }
    }

    @Test
    public void exprTest1() {
        SyntaxTree parsed = minus(constant(12));
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
                constant(1.1f),
                minus(constant(13)),
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
                constant(1),
                new BinOpNode(
                        new BinOpNode(
                                constant(13),
                                minus(constant(1)),
                                Token.TIMES
                        ),
                        new BinOpNode(
                                minus(constant(1.12f)),
                                minus(constant(3)),
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
                                constant(1),
                                constant(13.7f),
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
        SyntaxTree exprTree5 = constant(1.1f);

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
                        constant(1),
                        constant(2),
                        Token.MOD
                ),
                constant(3),
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
                constant(12),
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
        // it just has to parse, not evaluateProgram reasonably
        SyntaxTree tree = new BinOpNode(
                constant(true),
                constant(false),
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
                constant(true),
                constant(false)
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
                constant(true),
                constant(false)
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
                constant(true),
                new BinOpNode(
                        new UnaryOpNode(
                                new VariableEvalNode(Token.ID("someBool")),
                                Token.NOT
                        ),
                        constant(false),
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
                        constant(false),
                        Token.OR
                ),
                constant(true),
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
                        constant(false),
                        Token.AND
                ),
                constant(true),
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
                constant(true),
                new BinOpNode(
                        new UnaryOpNode(
                                new VariableEvalNode(Token.ID("someBool")),
                                Token.NOT
                        ),
                        constant(false),
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
        Token<String> progName = Token.ID("testProg");
        ProgramNode programNode = new ProgramNode(progName,
                new BlockNode(
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
        Token<String> progName = Token.ID("testProg");

        ProgramNode programNode = new ProgramNode(progName, new BlockNode(
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
        Token<String> progName = Token.ID("testProg");
        ProgramNode programNode = new ProgramNode(progName, new BlockNode(
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

        try {
            doParseProgramTest(text, programNode);
            assertThat("Shouldn't have gotten here", true, is(false));
        } catch (UnexpectedTokenException ise) {
            assertThat(ise.getMessage(),
                    is(String.format("Unexpected token type %s", Token.Type.ELSE.name())));
        }
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
                        constant(1),
                        new BinOpNode(
                                constant(3),
                                constant(2),
                                Token.PLUS
                        ),
                        Token.GREATER_THAN
                ),
                new BinOpNode(
                        constant(3),
                        constant(4),
                        Token.TIMES
                ),
                Token.GREATER_THAN
        );

        doParseExpressionTest(text, tree);
    }

    @Test
    public void progTest1() {
        Token<String> progName = Token.ID("test1");

        // standard empty program
        ProgramNode progTree1 = new ProgramNode(
                progName,
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
        Token<String> progName = Token.ID("test2");

        // declare some variables, do nothing
        ProgramNode progTree2 = new ProgramNode(
                progName,
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
        Token<String> progName = Token.ID("test3");

        ProgramNode progTree3 = new ProgramNode(progName, new BlockNode(
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
                                                constant(12),
                                                minus(constant(12)),
                                                Token.TIMES
                                        ),
                                        constant(4),
                                        Token.PLUS
                                )
                        ),
                        new AssignNode(
                                new VariableAssignNode(Token.ID("b")),
                                new BinOpNode(
                                        constant(1),
                                        new BinOpNode(
                                                constant(12),
                                                new VariableEvalNode(Token.ID("a")),
                                                Token.TIMES
                                        ),
                                        Token.MINUS
                                )
                        ),
                        new AssignNode(
                                new VariableAssignNode(Token.ID("a")),
                                constant(1)
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
        Token<String> progName = Token.ID("test4");
        Token<String> procName = Token.ID("proc1");

        ProgramNode progTree4 = new ProgramNode(progName,
                new BlockNode(
                        new DeclarationNode(
                                ImmutableList.of(
                                        new VariableDeclarationNode(ImmutableList.of(Token.ID("a")),
                                                TypeSpec.INTEGER),
                                        new VariableDeclarationNode(
                                                ImmutableList.of(Token.ID("b"), Token.ID("c")),
                                                TypeSpec.REAL)
                                ),
                                ImmutableList.of(
                                        new ProcedureDeclarationNode(procName,
                                                new BlockNode(
                                                        new DeclarationNode(
                                                                ImmutableList.of(
                                                                        new VariableDeclarationNode(
                                                                                ImmutableList
                                                                                        .of(Token
                                                                                                .ID("a")),
                                                                                TypeSpec.REAL),
                                                                        new VariableDeclarationNode(
                                                                                ImmutableList
                                                                                        .of(Token
                                                                                                .ID("d")),
                                                                                TypeSpec.REAL)
                                                                ),
                                                                ImmutableList.of()
                                                        ),
                                                        new CompoundNode(ImmutableList.of(
                                                                new AssignNode(
                                                                        new VariableAssignNode(
                                                                                Token.ID("a")),
                                                                        constant(1)),
                                                                new AssignNode(
                                                                        new VariableAssignNode(
                                                                                Token.ID("d")),
                                                                        constant(4))
                                                        ))
                                                )
                                        )
                                )
                        ),
                        new CompoundNode(ImmutableList.of(
                                new AssignNode(
                                        new VariableAssignNode(Token.ID("a")),
                                        constant(1)
                                ),
                                new AssignNode(
                                        new VariableAssignNode(Token.ID("b")),
                                        constant(2.0f)
                                ),
                                new AssignNode(
                                        new VariableAssignNode(Token.ID("c")),
                                        constant(3)
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
        Token<String> progName = Token.ID("test5");

        SyntaxTree progTree5 = new ProgramNode(Token.ID("test5"), new BlockNode(
                new DeclarationNode(
                        list(
                                new VariableDeclarationNode(varList("a", "b"), TypeSpec.BOOLEAN),
                                new VariableDeclarationNode(varList("c"), TypeSpec.BOOLEAN),
                                new VariableDeclarationNode(varList("d"), TypeSpec.REAL)
                        ), list() // no procedures here
                ),
                new CompoundNode(list(
                        new AssignNode(new VariableAssignNode(Token.ID("a")), constant(true)),
                        new AssignNode(new VariableAssignNode(Token.ID("b")), constant(false)),
                        new AssignNode(
                                new VariableAssignNode(Token.ID("c")),
                                new BinOpNode(
                                        new VariableEvalNode(Token.ID("a")),
                                        new VariableEvalNode(Token.ID("b")),
                                        Token.PLUS
                                )
                        ),
                        new AssignNode(
                                new VariableAssignNode(Token.ID("d")),
                                new VariableEvalNode(Token.ID("yes")))
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

    @Test
    public void procCallTest1() {
        String proc1Text = "procedure proc1; var a: Real; begin a := 12; b := 3; end;";
        String proc2Text = "procedure proc2; var b: Integer; begin a:= 1; b := 3; end;";
        String proc3Text =
                "procedure proc3; var b: Integer; c: Boolean; begin c := True; if c then a := 3 else b := 3 end;";
        String progText = String.format(
                "program test1; var a, b: Integer; %s %s %s begin proc1(); proc1(); proc2(); proc3() end.",
                proc1Text, proc2Text, proc3Text
        );

        Token<String> progName = Token.ID("test1");

        // we already believe we can parse procedure definitions from earlier tests, no need
        // to type them all out again
        Token<String> proc1Name = Token.ID("proc1");
        ProcedureDeclarationNode proc1Node = Parser.parseProcedure(proc1Text);

        Token<String> proc2Name = Token.ID("proc2");
        ProcedureDeclarationNode proc2Node = Parser.parseProcedure(proc2Text);

        Token<String> proc3Name = Token.ID("proc3");
        ProcedureDeclarationNode proc3Node = Parser.parseProcedure(proc3Text);

        ProgramNode desired = new ProgramNode(
                progName,
                new BlockNode(
                        new DeclarationNode(
                                list(new VariableDeclarationNode(list(Token.ID("a"), Token.ID("b")),
                                        TypeSpec.INTEGER)),
                                list(proc1Node, proc2Node, proc3Node)
                        ),
                        new CompoundNode(
                                list(
                                        new ProcedureCallNode(proc1Name),
                                        new ProcedureCallNode(proc1Name),
                                        new ProcedureCallNode(proc2Name),
                                        new ProcedureCallNode(proc3Name)
                                )
                        )
                )
        );

        doParseProgramTest(new String[] {progText}, desired);
    }

    @Test
    public void whileLoopTest1() {
        String whileText = "while 1<2 do a:=1";
        WhileNode desired = new WhileNode(
                Parser.parseExpression("1<2"),
                Parser.parseStatement("a := 1")
        );
        doParseStatementTest(new String[] {whileText}, desired);
    }

    @Test
    public void whileLoopTest2() {
        String whileText = "while ((1<2) and (a>=b)) do while c<d do begin a:= 2; int:=3.2 end";
        WhileNode desired = new WhileNode(
                Parser.parseExpression("((1<2) and (a>=b))"),
                new WhileNode(
                        Parser.parseExpression("c<d"),
                        Parser.parseStatement("begin a:= 2; int:=3.2 end")
                )
        );
        doParseStatementTest(new String[] {whileText}, desired);
    }

    @Test
    public void doUntilLoopTest1() {
        String loopText = "do a := 1 until 1<2";
        DoUntilNode desired = new DoUntilNode(
                Parser.parseExpression("1<2"),
                Parser.parseStatement("a:=1")
        );
        doParseStatementTest(new String[] {loopText}, desired);
    }

    @Test
    public void doUntilLoopTest2() {
        /*
        fun fact: indentation and carriage returns are a positive good
            do
                do
                    while 1 < 2 do
                        b := -12
                until false
            until true

         */
        String loopText = "do do while 1<2 do b := -12 until false until true";
        DoUntilNode desired = new DoUntilNode(
                Parser.parseExpression("true"),
                new DoUntilNode(
                        Parser.parseExpression("false"),
                        Parser.parseStatement("while 1 < 2 do b := -12")
                )
        );
        doParseStatementTest(new String[] {loopText}, desired);
    }

    @Test
    public void loopControlTest1() {
        String loopText = "do begin break; continue; a := 12 end until 2 = 1";
        DoUntilNode desired = new DoUntilNode(
                Parser.parseExpression("2=1"),
                new CompoundNode(
                        list(
                                LoopControlNode.Break(),
                                LoopControlNode.Continue(),
                                Parser.parseStatement("a:=12")
                        )
                )
        );
        doParseStatementTest(new String[] {loopText}, desired);
    }

    @Test
    public void forLoopTest1() {
        String loopText = "for a:=30+12-pro to 12-6*b do begin end";
        ForNode desired = ForNode.Forward(
                (AssignNode) Parser.parseStatement("a:= 30+12-pro"),
                Parser.parseExpression("12-6*b"),
                Parser.parseStatement("begin end")
        );
        doParseStatementTest(new String[] {loopText}, desired);
    }

    @Test
    public void forLoopTest2() {
        String loopText = "for a:=30+12-pro downto 12-6*b do begin end";
        ForNode desired = ForNode.Backward(
                (AssignNode) Parser.parseStatement("a:= 30+12-pro"),
                Parser.parseExpression("12-6*b"),
                Parser.parseStatement("begin end")
        );
        doParseStatementTest(new String[] {loopText}, desired);
    }
}
