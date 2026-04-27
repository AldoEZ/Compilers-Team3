package mx.unam.fi.compilers.g5.team03.parser;

import mx.unam.fi.compilers.g5.team03.parser.*;
import java.util.ArrayList;
import java.util.List;

public class CanonicalCollection {
    /**
     * grammar -> complete grammar
     * closure -> closure function to assemble canonical elements
     * goTo -> goto function to assemble canonical elements
     * states -> states of canonical elements
     */
    private final Grammar grammar;
    private final Closure closure;
    private final Goto goTo;
    private final List<State> states;
    
    public CanonicalCollection(Grammar grammar, Closure closure, Goto goTo) {
        this.grammar = grammar;
        this.closure = closure;
        this.goTo = goTo;
        this.states = new ArrayList<>();
        
        buildCollection();
    }
    
    private void buildCollection() {
        List<Item> startItems = new ArrayList<>();
        Production firstProduction = grammar.getProductionByNumber(0);
        
        if(firstProduction == null)
            throw new IllegalStateException("Initial production was not found.");
        
        startItems.add(new Item(firstProduction, 0, "$"));
        
        List<Item> startClosure = closure.itemClosure(startItems);
        states.add(new State(0, startClosure));
        
        for(int i = 0; i < states.size(); i++) {
            State currentState = states.get(i);
            
            List<String> symbols = relevantSymbols(currentState);
            
            for(String symbol : symbols) {
                List<Item> gotoResult = goTo.itemGoto(currentState.getItems(), symbol);
                
                if (gotoResult.isEmpty()) continue;
                
                int existingStateId = existsState(gotoResult);
                
                if (existingStateId == -1) {
                    int newStateId = states.size();
                    State newState = new State(newStateId, gotoResult);
                    states.add(newState);
                    currentState.addTransition(symbol, newStateId);
                } else {
                    currentState.addTransition(symbol, existingStateId);
                }
            }
        }
    }
    
    private List<String> relevantSymbols(State state) {
        List<String> result = new ArrayList<>();
        
        for(Item item : state.getItems()) {
            String symbol = item.getSymbolAfterDot();
            
            if(symbol != null && !result.contains(symbol)) 
                result.add(symbol);
        }
        return result;
    }
    
    private int existsState(List<Item> items) {
        for(State state : states) {
            if(state.sameItems(items))
                return state.getId();
        }
        return -1;
    }
    
    public List<State> getStates() {
        return new ArrayList<>(states);
    }
    
    public void printCollection() {
        int totalItems = 0;
        for(State state : states) {
            //System.out.println(state);
            totalItems += state.getItems().size();
        }
        System.out.println("Total states: " + states.size());
        System.out.println("Total items: " + totalItems);
        System.out.println("Total transitions: " + 
            states.stream().mapToInt(s -> s.getTransitions().size()).sum());
    }
}
