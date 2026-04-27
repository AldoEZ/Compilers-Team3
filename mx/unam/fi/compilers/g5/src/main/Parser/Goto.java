package mx.unam.fi.compilers.g5.team03.parser;

import mx.unam.fi.compilers.g5.team03.parser.Closure;
import java.util.ArrayList;
import java.util.List;

public class Goto {
    /**
     * closure -> generate the closures after each goto
     */
    private final Closure closure;
    
    public Goto(Closure closure) {
        this.closure = closure;
    }
    
    public List<Item> itemGoto(List<Item> items, String symbol) {
        List<Item> movedItems = new ArrayList<>();
        
        for(Item item : items) {
            String symbolAfterDot = item.getSymbolAfterDot();
            
            if(symbolAfterDot != null && symbolAfterDot.equals(symbol))
                movedItems.add(item.moveDot());
        }
        if(movedItems.isEmpty()) return movedItems;
        
        return closure.itemClosure(movedItems);
    }
    
    public void printGoto(List<Item> items, String symbol) {
        List<Item> result = itemGoto(items, symbol);
        
        System.out.println("GOTO with symbol: " + symbol);
        for(Item item : result) {
            System.out.println(item);
        }
    }
}
