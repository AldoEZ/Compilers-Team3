package mx.unam.fi.compilers.g5.team03.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * class that stores the productions individually
 */
public class Production {
    /**
     * number -> number of production
     * lhs -> left-hand side (symbol that produces)
     * rhs -> right-hand side (symbol that is produced)
     */
    private final int number;
    private final String lhs;
    private final List<String> rhs;
        
    public Production(int number, String lhs, List<String> rhs) {
        this.number = number;
        this.lhs = lhs;
        this.rhs = new ArrayList<>(rhs);
    }
    
    public int getNumber() {
        return number;
    }
    
    public String getLhs() {
        return lhs;
    }
    
    public List<String> getRhs() {
        return new ArrayList<>(rhs);
    }
    
    @Override
    public String toString() {
        return number + " " + lhs + " -> " + String.join(" ", rhs);
    }
}
