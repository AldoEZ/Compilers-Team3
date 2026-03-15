package mx.unam.fi.compilers.g5.team03;

import mx.unam.fi.compilers.g5.team03.TokenType;

/**
 * Class that functions as a token
 * and stores the type and the 
 * lexeme to witch it belongs
 */
public class Token {
    private final TokenType type;
    private final String lexeme;
    
    public Token(TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }
    
    public TokenType getType() {
        return this.type;
    }
    public String getLexeme() {
        return this.lexeme;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Token{type=%s, lexeme='%s'}",
            this.type, this.lexeme
        );
    }
}
