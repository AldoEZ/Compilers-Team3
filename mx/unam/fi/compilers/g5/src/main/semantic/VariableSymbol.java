package mx.unam.fi.compilers.g5.team03.semantic;

public class VariableSymbol {
    /**
     * name -> variable name
     * type -> semantic type of the variable
     */
    private final String name;
    private final String type;
    
    public VariableSymbol(String name, String type) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return name + " : " + type;
    }
}
