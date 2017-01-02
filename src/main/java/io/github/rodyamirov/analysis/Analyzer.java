package io.github.rodyamirov.analysis;

import com.google.common.collect.ImmutableList;
import io.github.rodyamirov.parse.Parser;
import io.github.rodyamirov.symbols.ScopeAssigner;
import io.github.rodyamirov.symbols.SymbolTable;
import io.github.rodyamirov.symbols.SymbolTableBuilder;
import io.github.rodyamirov.tree.ProgramNode;

import java.util.Collections;
import java.util.List;

/**
 * Created by richard.rast on 1/2/17.
 */
public final class Analyzer {
    // TODO - test this class a lot
    public static Program setup(String programText) throws AnalyzerException {
        ProgramNode programNode;

        try {
            programNode = Parser.parseProgram(programText);
        } catch (IllegalStateException ise) {
            throw new AnalyzerException(ise.getMessage(), Collections.emptyList(), AnalysisStage.PARSING);
        }

        ScopeAssigner.assignScopes(ScopeAssigner.ROOT_SCOPE, programNode);
        SymbolTable symbolTable = SymbolTableBuilder.buildFrom(programNode);

        List<ErrorMessage> controlErrors = BreakChecker.check(programNode);

        if (! controlErrors.isEmpty()) {
            throw new AnalyzerException(
                    "Exception checking control statements",
                    controlErrors,
                    AnalysisStage.CONTROL_CHECKING);
        }

        List<ErrorMessage> typeErrors = TypeChecker.assignTypes(programNode, symbolTable);

        if (! typeErrors.isEmpty()) {
            throw new AnalyzerException(
                    "Exception checking type validity",
                    typeErrors,
                    AnalysisStage.TYPE_CHECKING
            );
        }

        return new Program(symbolTable, programNode);
    }

    private Analyzer() {
        // nope
    }

    public enum AnalysisStage {
        PARSING, CONTROL_CHECKING, TYPE_CHECKING
    }

    public static class AnalyzerException extends IllegalStateException {
        private final ImmutableList<ErrorMessage> errors;
        private final AnalysisStage analysisStage;

        public AnalyzerException(String message, List<ErrorMessage> errorMessages, AnalysisStage analysisStage) {
            super(message);
            this.errors = ImmutableList.copyOf(errorMessages);
            this.analysisStage = analysisStage;
        }

        public List<ErrorMessage> getErrorMessages() {
            return errors;
        }

        public AnalysisStage getAnalysisStage() {
            return analysisStage;
        }
    }
}
