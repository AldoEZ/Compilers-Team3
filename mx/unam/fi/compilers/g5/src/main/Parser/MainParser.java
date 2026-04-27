package mx.unam.fi.compilers.g5.team03.parser;

import mx.unam.fi.compilers.g5.team03.parser.*;
import java.io.IOException;

public class MainParser {
    public static void main(String[] args) {
        try {
            Grammar grammar = new Grammar("../../../doc/grammar/grammar.txt");
            FirstSet fs = new FirstSet(grammar);
            Closure closure = new Closure(grammar, fs);
            Goto goTo = new Goto(closure);
            CanonicalCollection lr = new CanonicalCollection(grammar, closure, goTo);
            LALRCollection lalr = new LALRCollection(lr.getStates());
            
            System.out.println("=== Canonical LR(1) Collection ===");
            lr.printCollection();
            
            System.out.println("=== LALR(1) Collection ===");
            lalr.printCollection();
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
