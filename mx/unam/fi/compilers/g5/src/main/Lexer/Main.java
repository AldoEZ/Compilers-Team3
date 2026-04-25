package mx.unam.fi.compilers.g5.team03.lexer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        int cntTokens = 1;
        
        try {
            List<Token> fileTokens = lexer.tokenizeFile("../../../doc/test/test.c");
            
            try (FileWriter fw = new FileWriter("../../../doc/tokens/tokens.txt", false)) {
                for (Token token : fileTokens) {
                    String content = cntTokens + "\t" + token + "\n";
                    fw.write(content);
                    cntTokens++;
                }
            }
            
            System.out.println("File successfully written.");
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }
}
