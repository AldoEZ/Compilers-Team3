package mx.unam.fi.compilers.g5.team03.parser;

import mx.unam.fi.compilers.g5.team03.parser.*;
import java.util.ArrayList;
import java.util.List;

/**
 * class that produces the first set of the grammar
 */
public class FirstSet {
    /**
     * grammar -> stores the complete grammar
     * symbols -> stores non-terminals and terminal symbols
     * belongFirstSet ->matrix where the rows are symbols, the 
     * columns are terminals, indicates if a terminal belongs to the 
     * first set of a symbol
     */
    private final Grammar grammar;
    private final List<String> symbols;
    private final boolean[][] belongFirstSet;
    
    public FirstSet(Grammar grammar) {
        this.grammar = grammar;
        this.symbols = new ArrayList<>();
        
        initializeSymbols();
        
        belongFirstSet = new boolean[symbols.size()][grammar.getTerminals().size()];
        terminalsFirstSet();
        firstSets();
    }
    
    private void initializeSymbols() {
        for (String terminal : grammar.getTerminals()) {
            if (!symbols.contains(terminal)) {
                symbols.add(terminal);
            }
        }
        
        for (String nonTerminal : grammar.getNonTerminals()) {
            if (!symbols.contains(nonTerminal)) {
                symbols.add(nonTerminal);
            }
        }
    }
    
    private void terminalsFirstSet() {
        List<String> terminals = grammar.getTerminals();
        
        int terminalsSize = terminals.size();
        for (int i = 0; i < terminalsSize; i++) {
            String terminal = terminals.get(i);
            int symbolIndex = getSymbolIndex(terminal);
            
            if (symbolIndex != -1)
                belongFirstSet[symbolIndex][i] = true;
        }
    }
    
    private void firstSets() {
        boolean changed;
        
        do {
            changed = false;
            
            for (Production production : grammar.getProductions()) {
                String lhs = production.getLhs();
                List<String> rhs = production.getRhs();
                
                if (rhs.isEmpty()) continue;
                
                String firstSymbol = rhs.get(0);
                
                int lhsIndex = getSymbolIndex(lhs);
                int firstSymbolIndex = getSymbolIndex(firstSymbol);
                
                if (lhsIndex == -1 || firstSymbolIndex == -1) continue;
                
                int terminalsSize = grammar.getTerminals().size();
                for (int terminalCol = 0; terminalCol < terminalsSize; terminalCol++) {
                    if (belongFirstSet[firstSymbolIndex][terminalCol] && !belongFirstSet[lhsIndex][terminalCol]) {
                        belongFirstSet[lhsIndex][terminalCol] = true;
                        changed = true;
                    }
                }
            }
        } while (changed);
    }
    
    public List<String> getFirst(String symbol) {
        List<String> result = new ArrayList<>();
        
        int symbolIndex = getSymbolIndex(symbol);
        
        if (symbolIndex == -1) return result;
        
        List<String> terminals = grammar.getTerminals();
        int terminalsSize = terminals.size();
        
        for (int i = 0; i < terminalsSize; i++) {
            if (belongFirstSet[symbolIndex][i])
                result.add(terminals.get(i));
        }
        return result;
    }
    
    public List<String> getFirstOfSequence(List<String> sequence) {
        List<String> result = new ArrayList<>();
        
        if (sequence == null || sequence.isEmpty()) return result;
        
        String firstSymbol = sequence.get(0);
        return getFirst(firstSymbol);
    }
    
    private int getSymbolIndex(String symbol) {
        int symbolsSize = symbols.size();
        for (int i = 0; i < symbolsSize; i++) {
            if (symbols.get(i).equals(symbol))
                return i;
        }
        return -1;
    }
    public void printFirstSets() {
        for (String nonTerminal : grammar.getNonTerminals()) {
            List<String> first = getFirst(nonTerminal);
            System.out.println("FIRST(" + nonTerminal + ") = " + first);
        }
    }
}
