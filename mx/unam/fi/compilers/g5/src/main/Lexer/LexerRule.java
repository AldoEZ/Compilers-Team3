package mx.unam.fi.compilers.g5.team03;

import mx.unam.fi.compilers.g5.team03.*;
import java.util.regex.Pattern;

/**
 * Class that stores the rules
 * for each token type, and
 * whether the lexeme should be
 * ignored or not
 */
public class LexerRule {
    private final TokenType type;
    private final Pattern pattern;
    private final boolean ignored;
    
    public LexerRule(TokenType type, String regex, boolean ignored) {
        this.type = type;
        this.pattern = Pattern.compile(regex);
        this.ignored = ignored;
    }
    
    public TokenType getType() {
        return this.type;
    }
    public Pattern getPattern() {
        return this.pattern;
    }
    public boolean getIgnored() {
        return this.ignored;
    }
}
