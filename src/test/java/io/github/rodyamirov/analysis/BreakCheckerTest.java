package io.github.rodyamirov.analysis;

import com.google.common.collect.ImmutableList;
import io.github.rodyamirov.parse.Parser;
import io.github.rodyamirov.symbols.Scope;
import io.github.rodyamirov.symbols.ScopeAssigner;
import io.github.rodyamirov.tree.ProgramNode;
import io.github.rodyamirov.tree.StatementNode;
import io.github.rodyamirov.tree.SyntaxTree;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.rodyamirov.symbols.ScopeAssigner.ROOT_SCOPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 1/2/17.
 */
public class BreakCheckerTest {
    private ProgramNode makeProgram(Scope rootScope, String programText) {
        ProgramNode programNode = Parser.parseProgram(programText);
        ScopeAssigner.assignScopes(rootScope, programNode);
        return programNode;
    }

    private void checkBadNodes(List<ErrorMessage> errorMessages, SyntaxTree... badNodes) {
        assertThat("Right count", errorMessages.size(), is(badNodes.length));

        Set<SyntaxTree> desired = Arrays.stream(badNodes)
                .collect(Collectors.toSet());

        Set<SyntaxTree> actual = errorMessages.stream()
                .map(em -> em.problemNode)
                .collect(Collectors.toSet());

        assertThat("Got the right nodes", actual, is(desired));
    }

    private final ImmutableList<String> controlTypes = ImmutableList.of("continue", "break");

    @Test
    public void controlWithoutLoop1() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     %s"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            StatementNode badNode = programNode.blockNode.compoundNode.statements.get(0);

            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat(errorMessages.size(), is(1));
            assertThat(errorMessages.get(0).problemNode, is(badNode));
        }
    }

    @Test
    public void controlOutsideLoop1() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     for a:=1 to 10 do b := 12;"
                    + "     %s;"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            StatementNode badNode = programNode.blockNode.compoundNode.statements.get(1);

            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat(errorMessages.size(), is(1));
            assertThat(errorMessages.get(0).problemNode, is(badNode));
        }
    }

    @Test
    public void controlOutsideLoop2() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     for a:=1 downto 10 do b := 12;"
                    + "     %s;"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            StatementNode badNode = programNode.blockNode.compoundNode.statements.get(1);

            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat(errorMessages.size(), is(1));
            assertThat(errorMessages.get(0).problemNode, is(badNode));
        }
    }

    @Test
    public void controlOutsideLoop3() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     while true do a := 12;"
                    + "     %s;"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            StatementNode badNode = programNode.blockNode.compoundNode.statements.get(1);

            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat(errorMessages.size(), is(1));
            assertThat(errorMessages.get(0).problemNode, is(badNode));
        }
    }

    @Test
    public void controlOutsideLoop4() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     do a := a+3 until false;" // <-- not evaluating here
                    + "     %s;"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            StatementNode badNode = programNode.blockNode.compoundNode.statements.get(1);

            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat(errorMessages.size(), is(1));
            assertThat(errorMessages.get(0).problemNode, is(badNode));
        }
    }

    @Test
    public void controlOutsideLoop5() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     %s;"
                    + "     for a:=1 to 10 do b := 12;"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            StatementNode badNode = programNode.blockNode.compoundNode.statements.get(0);

            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat(errorMessages.size(), is(1));
            assertThat(errorMessages.get(0).problemNode, is(badNode));
        }
    }

    @Test
    public void controlOutsideLoop6() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     %s;"
                    + "     for a:=1 downto 10 do b := 12;"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            StatementNode badNode = programNode.blockNode.compoundNode.statements.get(0);

            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat(errorMessages.size(), is(1));
            assertThat(errorMessages.get(0).problemNode, is(badNode));
        }
    }

    @Test
    public void controlOutsideLoop7() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     %s;"
                    + "     while true do a := 12;"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            StatementNode badNode = programNode.blockNode.compoundNode.statements.get(0);

            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat(errorMessages.size(), is(1));
            assertThat(errorMessages.get(0).problemNode, is(badNode));
        }
    }

    @Test
    public void controlOutsideLoop8() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     %s;"
                    + "     do a := a+3 until false;" // <-- not evaluating here
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            StatementNode badNode = programNode.blockNode.compoundNode.statements.get(0);

            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat(errorMessages.size(), is(1));
            assertThat(errorMessages.get(0).problemNode, is(badNode));
        }
    }

    @Test
    public void multipleErrors1() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     %s; %s;"
                    + "     for a:=1 to 10 do b := 12;"
                    + "     %s; %s"
                    + " end .";
            programText = String.format(programText, controlType, controlType, controlType, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            List<StatementNode> statementNodes = programNode.blockNode.compoundNode.statements;

            checkBadNodes(BreakChecker.check(programNode),
                    statementNodes.get(0), statementNodes.get(1),
                    statementNodes.get(3), statementNodes.get(4));
        }
    }

    @Test
    public void multipleErrors2() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     %s; %s;"
                    + "     for a:=1 downto 10 do b := 12;"
                    + "     %s; %s;"
                    + "     for a:= 3 to 15 do b:= 13"
                    + " end .";
            programText = String.format(programText, controlType, controlType, controlType, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            List<StatementNode> statementNodes = programNode.blockNode.compoundNode.statements;

            checkBadNodes(BreakChecker.check(programNode),
                    statementNodes.get(0), statementNodes.get(1),
                    statementNodes.get(3), statementNodes.get(4));
        }
    }

    @Test
    public void multipleErrors3() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     %s; %s;"
                    + "     while true do a := 13.0;"
                    + "     %s; %s"
                    + " end .";
            programText = String.format(programText, controlType, controlType, controlType, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            List<StatementNode> statementNodes = programNode.blockNode.compoundNode.statements;

            checkBadNodes(BreakChecker.check(programNode),
                    statementNodes.get(0), statementNodes.get(1),
                    statementNodes.get(3), statementNodes.get(4));
        }
    }

    @Test
    public void multipleErrors4() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     %s; %s;"
                    + "     do break until false;"
                    + "     %s; %s"
                    + " end .";
            programText = String.format(programText, controlType, controlType, controlType, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            List<StatementNode> statementNodes = programNode.blockNode.compoundNode.statements;

            checkBadNodes(BreakChecker.check(programNode),
                    statementNodes.get(0), statementNodes.get(1),
                    statementNodes.get(3), statementNodes.get(4));
        }
    }

    @Test
    public void validControl1() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     do begin"
                    + "         a := 3;"
                    + "         %s"
                    + "     end until false"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat("All clear", errorMessages.size(), is(0));
        }
    }

    @Test
    public void validControl2() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     while true do begin"
                    + "         a := 3;"
                    + "         %s"
                    + "     end"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat("All clear", errorMessages.size(), is(0));
        }
    }

    @Test
    public void validControl3() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     for a:=1 to 12 do begin"
                    + "         a := 3;"
                    + "         %s"
                    + "     end"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat("All clear", errorMessages.size(), is(0));
        }
    }

    @Test
    public void validControl4() {
        for (String controlType : controlTypes) {
            String programText = ""
                    + " program test;"
                    + " var a:integer;"
                    + " begin"
                    + "     for a:=12 downto 1 do begin"
                    + "         a := 3;"
                    + "         %s"
                    + "     end"
                    + " end .";
            programText = String.format(programText, controlType);

            ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
            List<ErrorMessage> errorMessages = BreakChecker.check(programNode);

            assertThat("All clear", errorMessages.size(), is(0));
        }
    }

    @Test
    public void procCallBreakTest() {
        String programText;

        // the break is inside a loop, but the loop is outside the procedure, so break shouldn't
        // be allowed
        programText = ""
                + "program badBreakTest;"
                + "procedure um;"
                + " begin break end;"
                + "begin {program now}"
                + " while true do"
                + "     um()" // one statement loop
                + "end .";

        ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
        StatementNode badNode = programNode
                .blockNode.declarationNode.procedureDeclarations.get(0)
                .blockNode.compoundNode.statements.get(0);

        List<ErrorMessage> errors = BreakChecker.check(programNode);

        assertThat(errors.size(), is(1));
        assertThat(errors.get(0).problemNode, is(badNode));

        // but also check that breaks inside procedure calls are still OK
        programText = ""
                + "program goodBreakTest;"
                + "procedure um;"
                + " begin while true do break end;"
                + "begin um() end .";

        programNode = makeProgram(ROOT_SCOPE, programText);
        assertThat("Everything is fine this time", BreakChecker.check(programNode).size(), is(0));
    }

    @Test
    public void procCallContinueTest() {
        String programText;

        programText = ""
                + "program badBreakTest;"
                + "procedure um;"
                + " begin continue end;"
                + "begin {program now}"
                + " while true do"
                + "     um()" // one statement loop
                + "end .";

        ProgramNode programNode = makeProgram(ROOT_SCOPE, programText);
        StatementNode badNode = programNode
                .blockNode.declarationNode.procedureDeclarations.get(0)
                .blockNode.compoundNode.statements.get(0);

        List<ErrorMessage> errors = BreakChecker.check(programNode);

        assertThat(errors.size(), is(1));
        assertThat(errors.get(0).problemNode, is(badNode));

        // but some continues are ok
        programText = ""
                + "program badBreakTest;"
                + "procedure um;"
                + " begin do continue until true end;"
                + "begin {program now}"
                + " um()"
                + "end .";

        programNode = makeProgram(ROOT_SCOPE, programText);
        assertThat("Everything is fine this time", BreakChecker.check(programNode).size(), is(0));
    }
}
