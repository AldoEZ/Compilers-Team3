package mx.unam.fi.compilers.g5.team03.parser;

import mx.unam.fi.compilers.g5.team03.parser.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Grammar grammar = new Grammar("../../../doc/grammar/grammar.txt");
            grammar.printGrammar();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
