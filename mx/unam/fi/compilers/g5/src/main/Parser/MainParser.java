package mx.unam.fi.compilers.g5.team03.parser;

import mx.unam.fi.compilers.g5.team03.parser.*;
import java.io.IOException;
import java.util.List;

public class MainParser {
    public static void main(String[] args) {
        try {
            Grammar grammar = new Grammar("../../../doc/grammar/grammar.txt");
            FirstSet fs = new FirstSet(grammar);
            Closure closure = new Closure(grammar, fs);
            Goto goTo = new Goto(closure);
            
            CanonicalCollection lr = new CanonicalCollection(grammar, closure, goTo);
            LALRCollection lalr = new LALRCollection(lr.getStates());
            ParseTable parseTable = new ParseTable(grammar, lalr.getStates());
            
            ShiftReduceParser parser = new ShiftReduceParser(grammar, parseTable);
            List<ParserToken> tokens = parser.readTokens("../../../doc/tokens/tokens.txt");
            
            ParseTreeNode root = parser.parse(tokens);
            parser.printSteps();
            System.out.println("\n=== PARSE TREE ===");
            System.out.println(root.prettyPrint());
            
            ParseTreeViewer.showTree(root);
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}
