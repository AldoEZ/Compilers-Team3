package mx.unam.fi.compilers.g5.team03.parser;

public class ParserToken {
    /**
     * terminal -> terminal symbol for parser
     * lexeme -> original lexeme from lexer output
     */
    private final String terminal;
    private final String lexeme;
    
    public ParserToken(String terminal, String lexeme) {
        this.terminal = terminal;
        this.lexeme = lexeme;
    }
    
    public String getTerminal() {
        return terminal;
    }
    
    public String getLexeme() {
        return lexeme;
    }
    
    @Override
    public String toString() {
        return "(" + terminal + ", " + lexeme + ")";
    }
}
