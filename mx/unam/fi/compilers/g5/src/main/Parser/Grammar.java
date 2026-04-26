package mx.unam.fi.compilers.g5.team03.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * class that stores the complete grammar, 
 * and makes use of the production class
 */
public class Grammar {
    /**
     * class that stores the productions individually
     */
    public static class Production {
        /**
         * number -> number of production
         * lhs -> left-hand side (symbol that produces)
         * rhs -> right-hand side (symbol that is produced)
         */
        private final int number;
        private final String lhs;
        private final List<String> rhs;
        
        
        public Production(int number, String lhs, List<String> rhs) {
            this.number = number;
            this.lhs = lhs;
            this.rhs = new ArrayList<>(rhs);
        }
        
        public int getNumber() {
            return number;
        }
        
        public String getLhs() {
            return lhs;
        }
        
        public List<String> getRhs() {
            return new ArrayList<>(rhs);
        }
        
        @Override
        public String toString() {
            return number + " " + lhs + " -> " + String.join(" ", rhs);
        }
    }
    /**
     * terminals -> terminal symbols
     * nonTerminals -> non-terminal symbols
     * productions -> a complete production
     * startSymbol -> initial symbol
     */
    private final List<String> terminals;
    private final List<String> nonTerminals;
    private final List<Production> productions;
    private final String startSymbol;
    
    public Grammar(String filePath) throws IOException {
        terminals = new ArrayList<>();
        nonTerminals = new ArrayList<>();
        productions = new ArrayList<>();
        
        readGrammarFromFile(filePath);
        
        if (productions.isEmpty()) {
            throw new IllegalStateException("The grammar file is empty.");
        }
        
        startSymbol = productions.get(0).getLhs();
        inferTerminals();
        addEndMarker();
    }
    
    private void readGrammarFromFile(String filePath) throws IOException {
        List<String[]> rawLines = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            
            while (line != null) {
                line = line.trim();
                
                if (!line.isEmpty()) {
                    String[] parsed = parseGrammarLine(line);
                    rawLines.add(parsed);
                    
                    String lhs = parsed[1];
                    if (!nonTerminals.contains(lhs)) {
                        nonTerminals.add(lhs);
                    }
                }
                
                line = br.readLine();
            }
        }
        
        for (String[] parsed : rawLines) {
            int number = Integer.parseInt(parsed[0]);
            String lhs = parsed[1];
            List<String> rhs = splitSymbols(parsed[2]);
            
            productions.add(new Production(number, lhs, rhs));
        }
    }
    
    private String[] parseGrammarLine(String line) {
        String[] sides = line.split("->");
        
        if (sides.length != 2) {
            throw new IllegalStateException("Invalid production format: " + line);
        }
        
        String leftPart = sides[0].trim();
        String rightPart = sides[1].trim();
        
        int firstSpace = leftPart.indexOf(' ');
        if (firstSpace == -1) {
            throw new IllegalStateException("Invalid production numbering: " + line);
        }
        
        String numberPart = leftPart.substring(0, firstSpace).trim();
        String lhsPart = leftPart.substring(firstSpace).trim();
        
        return new String[]{numberPart, lhsPart, rightPart};
    }
    
    private List<String> splitSymbols(String text) {
        List<String> symbols = new ArrayList<>();
        
        if (text.isEmpty()) {
            return symbols;
        }
        
        String[] parts = text.split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty()) {
                symbols.add(part);
            }
        }
        
        return symbols;
    }
    
    private void inferTerminals() {
        for (Production production : productions) {
            for (String symbol : production.getRhs()) {
                if (!nonTerminals.contains(symbol) && !terminals.contains(symbol)) {
                    terminals.add(symbol);
                }
            }
        }
    }
    
    private void addEndMarker() {
        if (!terminals.contains("$")) {
            terminals.add("$");
        }
    }
    
    public List<String> getTerminals() {
        return new ArrayList<>(terminals);
    }
    
    public List<String> getNonTerminals() {
        return new ArrayList<>(nonTerminals);
    }
    
    public List<Production> getProductions() {
        return new ArrayList<>(productions);
    }
    
    public String getStartSymbol() {
        return startSymbol;
    }
    
    public boolean isTerminal(String symbol) {
        return terminals.contains(symbol);
    }
    
    public boolean isNonTerminal(String symbol) {
        return nonTerminals.contains(symbol);
    }
    
    public List<Production> getProductionsByLhs(String lhs) {
        List<Production> result = new ArrayList<>();
        
        for (Production production : productions) {
            if (production.getLhs().equals(lhs)) {
                result.add(production);
            }
        }
        
        return result;
    }
    
    public Production getProductionByNumber(int number) {
        for (Production production : productions) {
            if (production.getNumber() == number) {
                return production;
            }
        }
        return null;
    }
    
    public void printGrammar() {
        System.out.println("Start symbol: " + startSymbol);
        
        System.out.println("\nNon-terminals:");
        for (String nonTerminal : nonTerminals) {
            System.out.println(nonTerminal);
        }
        
        System.out.println("\nTerminals:");
        for (String terminal : terminals) {
            System.out.println(terminal);
        }
        
        System.out.println("\nProductions:");
        for (Production production : productions) {
            System.out.println(production);
        }
    }
}
