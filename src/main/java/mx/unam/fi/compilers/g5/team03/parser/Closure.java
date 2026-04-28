package mx.unam.fi.compilers.g5.team03.parser;

import java.util.ArrayList;
import java.util.List;

public class Closure {
    /**
     * grammar -> stores the complete grammar
     * firstSet -> first sets from grammar
     */
    private final Grammar grammar;
    private final FirstSet firstSet;
    
    public Closure(Grammar grammar, FirstSet firstSet) {
        this.grammar = grammar;
        this.firstSet = firstSet;
    }
    
    public List<Item> itemClosure(List<Item> initialItems) {
        List<Item> closure = new ArrayList<>();
        
        for(Item item : initialItems)
            addNewItem(closure, item);
        
        boolean changed;
        
        do {
            changed = false;
            
            List<Item> currentItems = new ArrayList<>(closure);
            
            for(Item item : currentItems) {
                String symbolAfterDot = item.getSymbolAfterDot();
                
                if(symbolAfterDot == null) continue;
                
                if(!grammar.isNonTerminal(symbolAfterDot)) continue;
                
                List<String> lookaheads = lookAheads(item);
                List<Production> productions = grammar.getProductionsByLhs(symbolAfterDot);
                
                for(Production production : productions) {
                    for(String lookahead : lookaheads) {
                        Item newItem = new Item(production, 0, lookahead);
                        
                        if(addNewItem(closure, newItem)) changed = true;
                    }
                }
            }
        } while(changed);
        
        return closure;
    }
    
    private List<String> lookAheads(Item item) {
        List<String> beta = item.getBeta();
        
        if(beta.isEmpty()) {
            List<String> result = new ArrayList<>();
            result.add(item.getLookahead());
            return result;
        }
        return firstSet.getFirstOfSequence(beta);
    }
    
    private boolean addNewItem(List<Item> items, Item newItem) {
        for(Item item : items) {
            if (item.equals(newItem)) return false;
        }
        
        items.add(newItem);
        return true;
    }
    
    public void printClosure(List<Item> items) {
        for(Item item : items) {
            System.out.println(item);
        }
    }
}
