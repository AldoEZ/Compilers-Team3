package mx.unam.fi.compilers.g5.team03.main;

import mx.unam.fi.compilers.g5.team03.lexer.*;
import mx.unam.fi.compilers.g5.team03.parser.*;
import mx.unam.fi.compilers.g5.team03.semantic.SemanticAnalyzer;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            runLexer();
            
            Grammar grammar = new Grammar("doc/grammar/grammar.txt");
            FirstSet fs = new FirstSet(grammar);
            Closure closure = new Closure(grammar, fs);
            Goto goTo = new Goto(closure);
            
            CanonicalCollection lr = new CanonicalCollection(grammar, closure, goTo);
            LALRCollection lalr = new LALRCollection(lr.getStates());
            ParseTable parseTable = new ParseTable(grammar, lalr.getStates());
            
            ShiftReduceParser parser = new ShiftReduceParser(grammar, parseTable);
            List<ParserToken> tokens = parser.readTokens("doc/tokens/tokens.txt");
            
            ParseTreeNode root = parser.parse(tokens);
            
            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.analyze(root);
            analyzer.printErrors();
            
            ParseTreeViewer.showTree(root);
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private static void runLexer() throws IOException {
        Lexer lexer = new Lexer();
        List<Token> fileTokens = lexer.tokenizeFile("doc/test/test.c");
        
        try(FileWriter fw = new FileWriter("doc/tokens/tokens.txt", false)) {
            for(Token token : fileTokens) {
                if(token.getType() == TokenType.ERROR) {
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
