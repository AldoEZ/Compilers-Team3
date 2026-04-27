package mx.unam.fi.compilers.g5.team03.parser;

import mx.unam.fi.compilers.g5.team03.parser.*;
import java.io.IOException;

public class MainParser {
    public static void main(String[] args) {
        try {
            Grammar grammar = new Grammar("../../../doc/grammar/grammar.txt");
            FirstSet fs = new FirstSet(grammar);
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
