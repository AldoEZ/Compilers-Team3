package mx.unam.fi.compilers.g5.team03.parser;

import java.util.ArrayList;
import java.util.List;

public class LALRCollection {
    /**
     * canonicalStates -> canonical states of LR(1)
     * lalrStates -> new states for LALR(1)
     * oldToNew -> new state identifiers
     */
    private final List<State> canonicalStates;
    private final List<State> lalrStates;
    private final int[] oldToNew;
    
    public LALRCollection(List<State> canonicalStates) {
        this.canonicalStates = new ArrayList<>(canonicalStates);
        this.lalrStates = new ArrayList<>();
        this.oldToNew = new int[canonicalStates.size()];
        
        buildLALRCollection();
    }
    
    private void buildLALRCollection() {
        mergeStates();
        newTransitions();
    }
    
    private void mergeStates() {
        for(State oldState : canonicalStates) {
            int mergedIndex = findState(oldState);
            
            if(mergedIndex == -1) {
                int newId = lalrStates.size();
                State newState = new State(newId, oldState.getItems());
                lalrStates.add(newState);
                oldToNew[oldState.getId()] = newId;
            } else {
                State mergedState = lalrStates.get(mergedIndex);
                
                for(Item item : oldState.getItems())
                    mergedState.addItem(item);
                
                oldToNew[oldState.getId()] = mergedIndex;
            }
        }
    }
    
    private int findState(State target) {
        for(int i = 0; i < lalrStates.size(); i++) {
            if(lalrStates.get(i).sameCore(target)) return i;
        }
        return -1;
    }
    
    private void newTransitions() {
        for(State oldState : canonicalStates) {
            int newFrom = oldToNew[oldState.getId()];
            State mergedFrom = lalrStates.get(newFrom);
            
            for(Transition transition : oldState.getTransitions()) {
                int oldTarget = transition.getTargetState();
                int newTarget = oldToNew[oldTarget];
                
                mergedFrom.addTransition(transition.getSymbol(), newTarget);
            }
        }
    }
    
    public List<State> getStates() {
        return new ArrayList<>(lalrStates);
    }
    
    public int getMergedStateId(int oldStateId) {
        return oldToNew[oldStateId];
    }
    
    public void printCollection() {
        int totalItems = 0;
        for(State state : lalrStates) {
            //System.out.println(state);
            totalItems += state.getItems().size();
        }
        System.out.println("Total states: " + lalrStates.size());
        System.out.println("Total items: " + totalItems);
        System.out.println("Total transitions: " + 
            lalrStates.stream().mapToInt(s -> s.getTransitions().size()).sum());
    }
}
