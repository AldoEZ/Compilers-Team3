package mx.unam.fi.compilers.g5.team03.semantic;

import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
    /**
     * ranges -> stack of variable ranges
     * functions -> global function table
     */
    private final List<List<VariableSymbol>> ranges;
    private final List<FunctionSymbol> functions;
    
    public SymbolTable() {
        this.ranges = new ArrayList<>();
        this.functions = new ArrayList<>();
    }
    
    public void pushScope() {
        ranges.add(new ArrayList<>());
    }
    
    public void popScope() {
        if(ranges.isEmpty())
            throw new IllegalStateException("No scope to pop.");
        
        ranges.remove(ranges.size() - 1);
    }
    
    public void insertVariable(String name, String type) {
        if(ranges.isEmpty())
            throw new IllegalStateException("No active scope for variable declaration.");
        
        List<VariableSymbol> currentScope = ranges.get(ranges.size() - 1);
        
        for(VariableSymbol variable : currentScope) {
            if(variable.getName().equals(name)) {
                throw new IllegalStateException(
                    "Semantic error: variable redeclared in the same scope -> " + name
                );
            }
        }
        
        currentScope.add(new VariableSymbol(name, type));
    }
    
    public VariableSymbol lookupVariable(String name) {
        for(int i = ranges.size() - 1; i >= 0; i--) {
            List<VariableSymbol> currentScope = ranges.get(i);
            
            for(VariableSymbol variable : currentScope) {
                if (variable.getName().equals(name)) return variable;
            }
        }
        return null;
    }
    
    public VariableSymbol lookupVariableCurrentScope(String name) {
        if(ranges.isEmpty()) return null;
        
        List<VariableSymbol> currentScope = ranges.get(ranges.size() - 1);
        
        for(VariableSymbol variable : currentScope) {
            if(variable.getName().equals(name)) return variable;
        }
        return null;
    }
    
    public boolean existsVariable(String name) {
        return lookupVariable(name) != null;
    }
    
    public void insertFunction(String name, String returnType, int arity) {
        for(FunctionSymbol function : functions) {
            if(function.getName().equals(name)) {
                throw new IllegalStateException(
                    "Semantic error: function redeclared -> " + name
                );
            }
        }
        functions.add(new FunctionSymbol(name, returnType, arity));
    }
    
    public FunctionSymbol lookupFunction(String name) {
        for(FunctionSymbol function : functions) {
            if(function.getName().equals(name)) return function;
        }
        return null;
    }
    
    public boolean existsFunction(String name) {
        return lookupFunction(name) != null;
    }
    
    public boolean hasMainFunction() {
        return existsFunction("main");
    }
    
    public void printVariables() {
        System.out.println("=== VARIABLE SCOPES ===");
        
        for(int i = 0; i < ranges.size(); i++) {
            System.out.println("Scope " + i + ":");
            
            for(VariableSymbol variable : ranges.get(i))
                System.out.println("  " + variable);
        }
    }
    
    public void printFunctions() {
        System.out.println("=== FUNCTIONS ===");
        
        for(FunctionSymbol function : functions)
            System.out.println("  " + function);
    }
}
