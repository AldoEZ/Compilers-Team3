package mx.unam.fi.compilers.g5.team03.semantic;

public class FunctionSymbol {
    /**
     * name -> function name
     * returnType -> semantic return type
     * nParameters -> number of parameters
     */
    private final String name;
    private final String returnType;
    private final int nParameters;
    
    public FunctionSymbol(String name, String returnType, int nParameters) {
        this.name = name;
        this.returnType = returnType;
        this.nParameters = nParameters;
    }
    
    public String getName() {
        return name;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public int getArity() {
        return nParameters;
    }
    
    @Override
    public String toString() {
        return name + " : " + returnType + " (" + nParameters + ")";
    }
}
