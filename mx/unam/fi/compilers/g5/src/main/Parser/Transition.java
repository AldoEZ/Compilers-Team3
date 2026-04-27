package mx.unam.fi.compilers.g5.team03.parser;

public class Transition {
    /**
     * symbol ->  current symbol
     * targetState -> next state to go to
     */
    private final String symbol;
    private final int targetState;
    
    public Transition(String symbol, int targetState) {
        this.symbol = symbol;
        this.targetState = targetState;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public int getTargetState() {
        return targetState;
    }
    
    @Override
    public String toString() {
        return "--" + symbol + "--> I" + targetState;
    }
}
