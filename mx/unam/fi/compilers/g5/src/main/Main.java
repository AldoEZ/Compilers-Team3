package mx.unam.fi.compilers.g5.team03;

import mx.unam.fi.compilers.g5.team03.*;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        
        try {
            List<Token> fileTokens = lexer.tokenizeFile("../../doc/test/test.c");
            int countTokens = fileTokens.size();
            for (Token token : fileTokens)
                System.out.println(token);
            
            System.out.println("Total de tokens: " + countTokens);
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }
    }
}
