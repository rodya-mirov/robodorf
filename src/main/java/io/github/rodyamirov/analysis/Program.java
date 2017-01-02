package io.github.rodyamirov.analysis;

import io.github.rodyamirov.eval.EvalVisitor;
import io.github.rodyamirov.symbols.SymbolTable;
import io.github.rodyamirov.symbols.SymbolValueTable;
import io.github.rodyamirov.tree.ProgramNode;

/**
 * Created by richard.rast on 1/2/17.
 */
public class Program {
    private final SymbolTable symbolTable;
    private final ProgramNode programNode;

    public Program(SymbolTable symbolTable, ProgramNode programNode) {
        this.symbolTable = symbolTable;
        this.programNode = programNode;
    }

    public SymbolValueTable run() {
        return EvalVisitor.evaluateProgram(programNode, symbolTable);
    }
}
