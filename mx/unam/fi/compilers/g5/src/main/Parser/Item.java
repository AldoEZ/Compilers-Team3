package mx.unam.fi.compilers.g5.team03.parser;

import mx.unam.fi.compilers.g5.team03.parser.Production;
import java.util.List;

public class Item {
    /**
     * production -> individual production of grammar
     * dotPosition -> position of a dot on a rhs
     * lookahead -> LR(1) item anticipation terminal
     */
    private final Production production;
    private final int dotPosition;
    private final String lookahead;
    
    public Item(Production production, int dotPosition, String lookahead) {
        this.production = production;
        this.dotPosition = dotPosition;
        this.lookahead = lookahead;
    }
    
    public Production getProduction() {
        return production;
    }
    
    public int getDotPosition() {
        return dotPosition;
    }
    
    public String getLookahead() {
        return lookahead;
    }
    
    public boolean isComplete() {
        return dotPosition >= production.getRhs().size();
    }
    
    public String getSymbolAfterDot() {
        if(isComplete()) return null;
        
        return production.getRhs().get(dotPosition);
    }
    
    public List<String> getBeta() {
        if(isComplete() || dotPosition + 1 >= production.getRhs().size()) {
            return List.of();
        }
        return production.getRhs().subList(dotPosition + 1, production.getRhs().size());
    }
    
    public Item moveDot() {
        if(isComplete())
            throw new IllegalStateException("Cannot move dot in a complete item.");
        
        return new Item(production, dotPosition + 1, lookahead);
    }
    
    public boolean sameCore(Item other) {
        return production.getNumber() == other.production.getNumber()
            && dotPosition == other.dotPosition;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Item other))
            return false;
        
        return production.getNumber() == other.production.getNumber()
            && dotPosition == other.dotPosition
            && lookahead.equals(other.lookahead);
    }
    
    @Override
    public int hashCode() {
        int result = production.getNumber();
        result = 31 * result + dotPosition;
        result = 31 * result + lookahead.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[")
          .append(production.getLhs())
          .append(" -> ");
        
        List<String> rhs = production.getRhs();
        int rhsSize = rhs.size();        
        for(int i = 0; i < rhsSize; i++) {
            if (i == dotPosition)
                sb.append(". ");
            
            sb.append(rhs.get(i)).append(" ");
        }
        
        if(dotPosition == rhs.size())
            sb.append(". ");
        
        sb.append(", ").append(lookahead).append("]");
        return sb.toString();
    }
}
