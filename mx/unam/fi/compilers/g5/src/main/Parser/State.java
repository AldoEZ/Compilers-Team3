package mx.unam.fi.compilers.g5.team03.parser;

import mx.unam.fi.compilers.g5.team03.parser.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class State {
    /**
     * id -> state identifier
     * items -> items from state
     * transitions -> transitions between canonical elements
     */
    private final int id;
    private final List<Item> items;
    private final List<Transition> transitions;
    
    public State(int id, List<Item> items) {
        this.id = id;
        this.items = new ArrayList<>(items);
        this.transitions = new ArrayList<>();
        sortItems(this.items);
    }
    
    public int getId() {
        return id;
    }
    
    public List<Item> getItems() {
        return new ArrayList<>(items);
    }
    
    public List<Transition> getTransitions() {
        return new ArrayList<>(transitions);
    }
    
    public void addTransition(String symbol, int targetState) {
        for(Transition transition : transitions) {
            if(transition.getSymbol().equals(symbol)) {
                if(transition.getTargetState() != targetState) {
                    throw new IllegalStateException(
                        "Conflict in transition with symbol " + symbol
                        + " from state I" + id
                    );
                }
                return;
            }
        }
        transitions.add(new Transition(symbol, targetState));
    }
    
    private void sortItems(List<Item> itemList) {
        Collections.sort(itemList, (a, b) -> a.toString().compareTo(b.toString()));
    }
    
    public boolean sameItems(List<Item> otherItems) {
        if (items.size() != otherItems.size()) return false;
        
        List<Item> sortedOther = new ArrayList<>(otherItems);
        sortItems(sortedOther);
        
        for(int i = 0; i < items.size(); i++) {
            if(!items.get(i).equals(sortedOther.get(i))) return false;
        }
        return true;
    }
    
    public boolean sameCore(State other) {
        List<Item> otherItems = new ArrayList<>(other.getItems());
        
        if(items.size() != otherItems.size()) return false;
        
        sortItems(otherItems);
        
        for(int i = 0; i < items.size(); i++) {
            if(!items.get(i).sameCore(otherItems.get(i))) return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("I").append(id).append(":\n");
        
        for(Item item : items)
            sb.append(item).append("\n");
        
        if(!transitions.isEmpty()) {
            sb.append("Transitions:\n");
            for(Transition transition : transitions) {
                sb.append("I").append(id).append(" ").append(transition).append("\n");
            }
        }
        return sb.toString();
    }
    
    public void addItem(Item newItem) {
        for(Item item : items) {
            if(item.equals(newItem)) return;
        }
        items.add(newItem);
        sortItems(items);
    }
}
