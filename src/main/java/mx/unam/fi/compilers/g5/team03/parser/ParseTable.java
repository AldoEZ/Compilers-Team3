package mx.unam.fi.compilers.g5.team03.parser;

import mx.unam.fi.compilers.g5.team03.parser.*;
import java.util.ArrayList;
import java.util.List;

public class ParseTable {
    /**
     * grammar -> complete grammar
     * states -> LALR(1) states
     * actionTable -> table that defines the actions for shift and reduce
     * gotoTable -> table that defines the goto actions
     * ERROR -> value that indicates an error state
     * ACCEPT -> value that indicates an acceptance state
     */
    private final Grammar grammar;
    private final List<State> states;
    
    private final int[][] actionTable;
    private final int[][] gotoTable;
    
    private static final int ERROR = 0;
    private static final int ACCEPT = 999;
    
    /**
     * importants values of the action and goto
     * ACTION:
     * 0   -> error
     * 999 -> accept
     * > 0  -> shift to state
     * < 0  -> reduce by production (-value)
     * GOTO:
     * -1  -> no transition
     * >=0 -> target state
     */
    public ParseTable(Grammar grammar, List<State> states) {
        this.grammar = grammar;
        this.states = new ArrayList<>(states);
        
        actionTable = new int[states.size()][grammar.getTerminals().size()];
        gotoTable = new int[states.size()][grammar.getNonTerminals().size()];
        
        gotoTable();
        buildTables();
    }
    
    private void gotoTable() {
        for(int i = 0; i < gotoTable.length; i++) {
            for(int j = 0; j < gotoTable[i].length; j++)
                gotoTable[i][j] = -1;
        }
    }
    
    private void buildTables() {
        for (State state : states) {
            int stateId = state.getId();
            
            // 1. shift / goto from transitions
            for(Transition transition : state.getTransitions()) {
                String symbol = transition.getSymbol();
                int targetState = transition.getTargetState();
                
                if(grammar.isTerminal(symbol)) {
                    int terminalCol = terminalIndex(symbol);
                    setAction(stateId, terminalCol, targetState);
                } else if(grammar.isNonTerminal(symbol)) {
                    int nonTerminalCol = nonTerminalIndex(symbol);
                    setGoto(stateId, nonTerminalCol, targetState);
                }
            }
            
            // 2. reduce / accept from complete items
            for(Item item : state.getItems()) {
                if (!item.isComplete()) continue;
                
                Production production = item.getProduction();
                
                if(production.getNumber() == 0 && item.getLookahead().equals("$")) {
                    int eofCol = terminalIndex("$");
                    setAction(stateId, eofCol, ACCEPT);
                } else {
                    int lookaheadCol = terminalIndex(item.getLookahead());
                    setAction(stateId, lookaheadCol, -production.getNumber());
                }
            }
        }
    }
    
    private void setAction(int state, int terminalCol, int value) {
        int current = actionTable[state][terminalCol];
        
        if(current != ERROR && current != value) {
            throw new IllegalStateException(
                "ACTION conflict at state I" + state
                + ", terminal '" + grammar.getTerminals().get(terminalCol) + "'"
                + " -> current: " + actionToString(current)
                + ", new: " + actionToString(value)
            );
        }
        
        actionTable[state][terminalCol] = value;
    }
    
    private void setGoto(int state, int nonTerminalCol, int value) {
        int current = gotoTable[state][nonTerminalCol];
        
        if(current != -1 && current != value) {
            throw new IllegalStateException(
                "GOTO conflict at state I" + state
                + ", non-terminal '" + grammar.getNonTerminals().get(nonTerminalCol) + "'"
                + " -> current: " + current
                + ", new: " + value
            );
        }
        
        gotoTable[state][nonTerminalCol] = value;
    }
    
    private int terminalIndex(String terminal) {
        List<String> terminals = grammar.getTerminals();
        
        for(int i = 0; i < terminals.size(); i++) {
            if(terminals.get(i).equals(terminal)) return i;
        }
        return -1;
    }
    
    private int nonTerminalIndex(String nonTerminal) {
        List<String> nonTerminals = grammar.getNonTerminals();
        
        for(int i = 0; i < nonTerminals.size(); i++) {
            if(nonTerminals.get(i).equals(nonTerminal)) return i;
        }
        return -1;
    }
    
    public int getAction(int state, String terminal) {
        int col = terminalIndex(terminal);
        if(col == -1) return ERROR;
        
        return actionTable[state][col];
    }
    
    public int getGoto(int state, String nonTerminal) {
        int col = nonTerminalIndex(nonTerminal);
        if(col == -1) return -1;
        
        return gotoTable[state][col];
    }
    
    public String actionToString(int value) {
        if(value == ERROR) return "";
        
        if(value == ACCEPT) return "acc";
        
        if(value > 0) return "s" + value;
        
        return "r" + (-value);
    }
    
    public void printActionTable() {
        List<String> terminals = grammar.getTerminals();
        
        System.out.println("=== ACTION TABLE ===");
        System.out.print("State\t");
        
        for(String terminal : terminals)
            System.out.print(terminal + "\t");
        
        System.out.println();
        
        for(int i = 0; i < actionTable.length; i++) {
            System.out.print("I" + i + "\t");
            for (int j = 0; j < actionTable[i].length; j++)
                System.out.print(actionToString(actionTable[i][j]) + "\t");
            
            System.out.println();
        }
    }
    
    public void printGotoTable() {
        List<String> nonTerminals = grammar.getNonTerminals();
        
        System.out.println("=== GOTO TABLE ===");
        System.out.print("State\t");
        for(String nonTerminal : nonTerminals) {
            if(!nonTerminal.equals(grammar.getStartSymbol()))
                System.out.print(nonTerminal + "\t");
        }
        System.out.println();
        
        for(int i = 0; i < gotoTable.length; i++) {
            System.out.print("I" + i + "\t");
            for(int j = 0; j < gotoTable[i].length; j++) {
                if(!nonTerminals.get(j).equals(grammar.getStartSymbol())) {
                    int value = gotoTable[i][j];
                    System.out.print((value == -1 ? "" : value) + "\t");
                }
            }
            System.out.println();
        }
    }
    
    public void printSummary() {
        int actionEntries = 0;
        int gotoEntries = 0;
        
        for(int i = 0; i < actionTable.length; i++) {
            for(int j = 0; j < actionTable[i].length; j++) {
                if(actionTable[i][j] != ERROR) actionEntries++;
            }
        }
        
        for(int i = 0; i < gotoTable.length; i++) {
            for(int j = 0; j < gotoTable[i].length; j++) {
                if(gotoTable[i][j] != -1) gotoEntries++;
            }
        }
        
        System.out.println("=== PARSE TABLE SUMMARY ===");
        System.out.println("Total ACTION entries: " + actionEntries);
        System.out.println("Total GOTO entries: " + gotoEntries);
    }
}
