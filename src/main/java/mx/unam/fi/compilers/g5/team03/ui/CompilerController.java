package mx.unam.fi.compilers.g5.team03.ui;

import mx.unam.fi.compilers.g5.team03.lexer.*;
import mx.unam.fi.compilers.g5.team03.parser.*;
import mx.unam.fi.compilers.g5.team03.semantic.SemanticAnalyzer;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import java.util.Collection;
import java.util.Collections;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CompilerController {
    private static final Path TEST_FILE_PATH = Path.of("doc/test/test.c");
    private static final Path TOKENS_FILE_PATH = Path.of("doc/tokens/tokens.txt");
    private static final Path GRAMMAR_FILE_PATH = Path.of("doc/grammar/grammar.txt");
    
    private final MainView view;
    
    public CompilerController(MainView view) {
        this.view = view;
        connectEvents();
        connectHighlighting();
    }
    
    private void connectHighlighting() {
        CodeArea area = view.getCodeEditor();
        
        area.textProperty().addListener((obs, oldText, newText) -> {
            area.setStyleSpans(0, computeHighlighting(newText));
        });
    }
    
    private void connectEvents() {
        view.getRunButton().setOnAction(e -> runCompiler());
    }
    
    private void runCompiler() {
        clearOutputs();
        
        String sourceCode = view.getCodeEditor().getText();
        
        if(sourceCode == null || sourceCode.isBlank()) {
            view.getParserOutput().setText("No source code provided.");
            view.getSemanticOutput().setText("No semantic analysis executed.");
            view.getParseTreePane().setTree(null);
            return;
        }
        
        try {
            // 0 Sobrescribir test.c con lo que escribió el usuario
            writeTest(sourceCode);
            
            // 1 Lexer
            Lexer lexer = new Lexer();
            List<Token> tokens = lexer.tokenize(sourceCode);
            
            StringBuilder lexerText = new StringBuilder();
            List<ParserToken> parserTokens = new ArrayList<>();
            StringBuilder tokensFileText = new StringBuilder();
            
            int index = 1;
            for(Token token : tokens) {
                lexerText.append(index)
                            .append("\t")
                            .append(token.toString())
                            .append("\n");
                
                if(token.getType() == TokenType.ERROR) {
                    throw new IllegalStateException(
                        "Lexical error: invalid token -> " + token.getLexeme()
                    );
                }
                String normalized = normalizeTokenForParser(token);
                
                parserTokens.add(new ParserToken(normalized, token.getLexeme()));
                tokensFileText.append(normalized)
                                .append("\t")
                                .append(token.getLexeme())
                                .append("\n");
                index++;
            }
            parserTokens.add(new ParserToken("$", "$"));
            tokensFileText.append("$\t$\n");
            
            // 2 Sobrescribir tokens.txt con los tokens generados
            writeTokens(tokensFileText.toString());
            
            view.getLexerOutput().setText(lexerText.toString());
            
            // 3 Parser setup
            Grammar grammar = new Grammar(GRAMMAR_FILE_PATH.toString());
            FirstSet fs = new FirstSet(grammar);
            Closure closure = new Closure(grammar, fs);
            Goto goTo = new Goto(closure);
            
            CanonicalCollection lr = new CanonicalCollection(grammar, closure, goTo);
            LALRCollection lalr = new LALRCollection(lr.getStates());
            ParseTable parseTable = new ParseTable(grammar, lalr.getStates());
            
            // 4 Parse
            ShiftReduceParser parser = new ShiftReduceParser(grammar, parseTable);
            ParseTreeNode root = parser.parse(parserTokens);
            
            StringBuilder parserText = new StringBuilder();
            parserText.append("Parsing Success!\n\n");
            parserText.append("Total steps: ")
                    .append(parser.getSteps().size())
                    .append("\n");
            parserText.append("Parse completed without syntax errors.\n\n");
            
            for (String actionLine : parser.getImportantActions())
                parserText.append(actionLine).append("\n");
            
            view.getParserOutput().setText(parserText.toString());
            
            // 5 Semantic analysis
            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            boolean semanticOk = analyzer.analyze(root);
            
            if(semanticOk) {
                view.getSemanticOutput().setText("SDT Verified!");
            } else {
                StringBuilder semanticText = new StringBuilder();
                for(String error : analyzer.getErrors())
                    semanticText.append(error).append("\n");
                
                view.getSemanticOutput().setText(semanticText.toString());
            }
            // 6 Parse tree gráfico
            view.getParseTreePane().setTree(root);
        } catch (IOException ex) {
            view.getParserOutput().setText("I/O error: " + ex.getMessage());
            view.getSemanticOutput().setText("Semantic analysis aborted.");
            view.getParseTreePane().setTree(null);
        } catch (IllegalStateException ex) {
            view.getParserOutput().setText(ex.getMessage());
            view.getSemanticOutput().setText("Semantic analysis aborted.");
            view.getParseTreePane().setTree(null);
        } catch (Exception ex) {
            view.getParserOutput().setText("Unexpected error: " + ex.getMessage());
            view.getSemanticOutput().setText("Semantic analysis aborted.");
            view.getParseTreePane().setTree(null);
        }
    }
    
    private void clearOutputs() {
        view.getLexerOutput().clear();
        view.getParserOutput().clear();
        view.getSemanticOutput().clear();
        view.getParseTreePane().setTree(null);
    }
    
    private void writeTest(String sourceCode) throws IOException {
        Files.createDirectories(TEST_FILE_PATH.getParent());
        Files.writeString(TEST_FILE_PATH, sourceCode, StandardCharsets.UTF_8);
    }
    
    private void writeTokens(String tokensContent) throws IOException {
        Files.createDirectories(TOKENS_FILE_PATH.getParent());
        Files.writeString(TOKENS_FILE_PATH, tokensContent, StandardCharsets.UTF_8);
    }
    
    private String normalizeTokenForParser(Token token) {
        return switch (token.getType()) {
            case KEYWORD, OPERATOR, PUNCTUATOR -> token.getLexeme();
            case IDENTIFIER -> "id";
            case CONSTANT -> "constant";
            case LITERAL -> "literal";
            case ERROR -> "ERROR";
            default -> token.getLexeme();
        };
    }
    
    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize(text);
        
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        
        int cursor = 0;
        
        for(Token token : tokens) {
            if(token.getType() == TokenType.ERROR) continue;
            
            String lexeme = token.getLexeme();
            int start = text.indexOf(lexeme, cursor);
            
            if(start < 0) continue;
            
            int end = start + lexeme.length();
            
            if (start > cursor)
                spansBuilder.add(Collections.emptyList(), start - cursor);
            
            spansBuilder.add(
                Collections.singleton(getStyleClassForToken(token)),
                lexeme.length()
            );
            
            cursor = end;
        }
        if(cursor < text.length())
            spansBuilder.add(Collections.emptyList(), text.length() - cursor);
        
        return spansBuilder.create();
    }
    
    private String getStyleClassForToken(Token token) {
        return switch (token.getType()) {
            case KEYWORD -> "token-keyword";
            case IDENTIFIER -> "token-identifier";
            case CONSTANT -> "token-constant";
            case LITERAL -> "token-literal";
            case OPERATOR -> "token-operator";
            case PUNCTUATOR -> "token-punctuator";
            case ERROR -> "token-error";
            default -> "token-default";
        };
    }
}
