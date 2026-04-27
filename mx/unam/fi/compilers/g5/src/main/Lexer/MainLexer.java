package mx.unam.fi.compilers.g5.team03.lexer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainLexer {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        
        try {
            List<Token> fileTokens = lexer.tokenizeFile("../../../doc/test/test.c");
            
            try (FileWriter fw = new FileWriter("../../../doc/tokens/tokens.txt", false)) {
                for (Token token : fileTokens) {
                    if (token.getType() == TokenType.ERROR) {
                        throw new IllegalStateException(
                            "Lexical error: invalid token -> " + token.getLexeme()
                        );
                    }
                    
                    String terminal = normalizeTokenForParser(token);
                    String content = terminal + "\t" + token.getLexeme() + "\n";
                    fw.write(content);
                }
                
                fw.write("$\t$\n");
            }
            
            System.out.println("File successfully written.");
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private static String normalizeTokenForParser(Token token) {
        String lexeme = token.getLexeme();
        
        return switch (token.getType()) {
            case KEYWORD, OPERATOR, PUNCTUATOR -> lexeme;
            case IDENTIFIER -> "id";
            case CONSTANT -> "constant";
            case LITERAL -> "literal";
            case ERROR -> "ERROR";
        };
    }
}