package mx.unam.fi.compilers.g5.team03;

import mx.unam.fi.compilers.g5.team03.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

public class Lexer {
    private static final Set<String> KEYWORDS = Set.of(
    "auto", "break", "case", "char", "const", "continue", "default",
        "do", "double", "else", "enum", "extern", "float", "for", "goto",
        "if", "int", "long", "register", "return", "short", "signed",
        "sizeof", "static", "struct", "switch", "typedef", "union",
        "unsigned", "void", "volatile", "while"
    );
    
    //* lexical rules
    private final List<LexerRule> rules;
    
    public Lexer() {
        this.rules = new ArrayList<>();
        this.initializeRules();
    }
    
    private void initializeRules() {
        /**
         * non-tokens elements that are ignored
         * include comments (single-line and multi-line)
         * and whitespace
         */
        //* whitespace
        rules.add(new LexerRule(null, "\\s+", true));
        
        //* comments
        rules.add(new LexerRule(null, "//[^\\n\\r]*", true));
        rules.add(new LexerRule(null, "(?s)/\\*.*?\\*/", true));
        
        /**
         * tokes that the lexer
         * must recognize
         */
        //* literals (double quotation marks)
        rules.add(new LexerRule(
            TokenType.LITERAL,
            "\"([^\"\\\\]|\\\\.)*\"",
            false
        ));
        
        //* literals (single quotes)
        rules.add(new LexerRule(
            TokenType.LITERAL,
            "'([^'\\\\]|\\\\.)'",
            false
        ));
        
        //* constants (numerical)
        rules.add(new LexerRule(
            TokenType.CONSTANT,
            "0[xX][0-9a-fA-F]+"
            + "|[0-9]+\\.[0-9]*([eE][+-]?[0-9]+)?"
            + "|\\.[0-9]+([eE][+-]?[0-9]+)?"
            + "|[0-9]+[eE][+-]?[0-9]+"
            + "|0[0-7]+"
            + "|[0-9]+",
            false
        ));
        
        //* operators
        rules.add(new LexerRule(
            TokenType.OPERATOR,
            "\\+\\+|--|==|!=|<=|>=|&&|\\|\\||\\+=|-=|\\*=|/=|%=|&=|\\|=|\\^=|<<=|>>=|<<|>>|->|[+\\-*/%=&|^~!<>]",
            false
        ));
        
        //* punctuators
        rules.add(new LexerRule(
            TokenType.PUNCTUATOR,
            "[(){}\\[\\],;.:?]",
            false
        ));
        
        //* identifiers
        rules.add(new LexerRule(
            TokenType.IDENTIFIER,
            "[A-Za-z_][A-Za-z0-9_]*",
            false
        ));
    }
    
    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int position = 0, inputSize = input.length();
        
        while(position < inputSize) {
            boolean matched = false;
            
            for(LexerRule rule : rules) {
                Matcher matcher = rule.getPattern().matcher(input);
                matcher.region(position, inputSize);
                
                if(matcher.lookingAt()) {
                    matched = true;
                    String lexeme = matcher.group();
                    
                    if(!rule.getIgnored()) {
                        TokenType type = rule.getType();
                        
                        if(type == TokenType.IDENTIFIER && KEYWORDS.contains(lexeme))
                            type = TokenType.KEYWORD;
                        
                        tokens.add(new Token(type, lexeme));
                    }
                    position = matcher.end();
                    break;
                }
            }
            if(!matched) {
                String invalidLexeme = String.valueOf(input.charAt(position));
                tokens.add(new Token(TokenType.ERROR, invalidLexeme));
                position++;
            }
        }
        
        return tokens;
    }
    
    public List<Token> tokenizeFile(String filePath) throws IOException {
        String content = Files.readString(Path.of(filePath));
        return tokenize(content);
    }
}
