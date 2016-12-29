# Robodorf

(working title)

This is a partial interpreter for Pascal. Essentially, I was going through Ruslan Spivak's "Let's Build a Simple Interpreter" series<sup><a name="footnote1src">[[1]](#footnote1target)</a></sup> and building the associated logic in Java as I went.

I've added a few features that aren't in the tutorial yet (e.g. type checking) and lots of tests, but essentially this is just an implementation of that guide.

For me, this is part of a bigger project that will be on another repo, and which has nothing to do with Pascal. For you the reader, I hope this provides value on its own!

<a name="footnote1target">[[1]](#footnote1src)</a> As of writing, Ruslan's blog has 12 parts in the series; the first one is [here](https://ruslanspivak.com/lsbasi-part1/).

## Using the Program

If you want to run Pascal programs for some reason, there is currently no command line entry point. However, it can tokenize / parse / evaluate a restricted set of Pascal programs. You can do this in code as follows:

```
String program = // program as a string here
Parser parser = new Parser(program);
ProgramNode parseTree = parser.parseProgram();
SymbolValueTable endState = EvalVisitor.evaluateProgram(parseTree);
```

In the above, `parseTree` will give a parsed version of the program (tokenizing the string is called by the parser). `endState` is a representation of the memory of the program after evaluation has completed, so it will store the variables and the values they hold.

The code is hopefully self-documenting as there are not a huge number of comments. If you find a bug please let me know on the issues page, ideally with a failing test.

That said, unimplemented features are _not_ bugs -- just things I haven't gotten to yet. So for example you can parse a procedure declaration, so long as it doesn't have any arguments, but you can't parse a procedure call (or anything to do with functions). You can't evaluate any of that, including just setting up the various variable declarations.

However, math _is_ working; you can write basic arithmetic expressions of arbitrary complexity, with integer or float values, and it will all work great.