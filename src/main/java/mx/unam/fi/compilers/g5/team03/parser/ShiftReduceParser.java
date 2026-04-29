package mx.unam.fi.compilers.g5.team03.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ShiftReduceParser {
    /**
     * grammar -> complete grammar
     * parseTable -> ACTION/GOTO table
     * steps -> full parsing trace
     * importantActions -> compact parser actions for UI
     */
    private final Grammar grammar;
    private final ParseTable parseTable;
    private final List<String> steps;
    private final List<String> importantActions;
    
    public ShiftReduceParser(Grammar grammar, ParseTable parseTable) {
        this.grammar = grammar;
        this.parseTable = parseTable;
        this.steps = new ArrayList<>();
        this.importantActions = new ArrayList<>();
    }
    
    public List<String> getSteps() {
        return new ArrayList<>(steps);
    }
    
    public List<String> getImportantActions() {
        return new ArrayList<>(importantActions);
    }
    
    public List<ParserToken> readTokens(String filePath) throws IOException {
        List<ParserToken> tokens = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            
            while(line != null) {
                line = line.trim();
                
                if(!line.isEmpty()) {
                    String[] parts = line.split("\\t");
                    
                    if(parts.length >= 2) {
                        String terminal = parts[0].trim();
                        String lexeme = parts[1].trim();
                        tokens.add(new ParserToken(terminal, lexeme));
                    }
                }
                line = br.readLine();
            }
        }
        
        if(tokens.isEmpty() || !tokens.get(tokens.size() - 1).getTerminal().equals("$"))
            tokens.add(new ParserToken("$", "$"));
        
        return tokens;
    }
    
    public ParseTreeNode parse(List<ParserToken> inputTokens) {
        steps.clear();
        importantActions.clear();
        
        Stack<Integer> stateStack = new Stack<>();
        Stack<String> symbolStack = new Stack<>();
        Stack<ParseTreeNode> treeStack = new Stack<>();
        
        stateStack.push(0);
        
        int index = 0;
        
        while(true) {
            int currentState = stateStack.peek();
            ParserToken currentToken = inputTokens.get(index);
            
            int action = parseTable.getAction(currentState, currentToken.getTerminal());
            
            steps.add(
                "StateStack=" + stateStack
                + " SymbolStack=" + symbolStack
                + " Input=" + remainingInput(inputTokens, index)
                + " Action=" + parseTable.actionToString(action)
            );
            
            if(action == 0) {
                List<String> expectedTokens = getExpectedTokens(currentState);
                String expectedText = getMostProbableExpectedToken(expectedTokens);
                
                throw new IllegalStateException(
                    "Parsing error... Unexpected token: " + currentToken
                    + " in state I" + currentState
                    + "\nExpected: " + expectedText
                );
            }
            
            if(action == 999) {
                if(treeStack.isEmpty())
                    throw new IllegalStateException("Parsing finished but parse tree is empty.");
                
                importantActions.add("Accept");
                System.out.println("Parsing Success!");
                return treeStack.peek();
            }
            
            if(action > 0) {
                symbolStack.push(currentToken.getTerminal());
                stateStack.push(action);
                
                ParseTreeNode terminalNode =
                    new ParseTreeNode(currentToken.getTerminal(), currentToken.getLexeme());
                treeStack.push(terminalNode);
                
                importantActions.add("Shift " + currentToken.getTerminal());
                index++;
            } else {
                int productionNumber = -action;
                Production production = grammar.getProductionByNumber(productionNumber);
                
                if(production == null) {
                    throw new IllegalStateException(
                        "Reduction error: production " + productionNumber + " not found."
                    );
                }
                
                List<String> rhs = production.getRhs();
                List<ParseTreeNode> children = new ArrayList<>();
                
                for(int i = 0; i < rhs.size(); i++) {
                    symbolStack.pop();
                    stateStack.pop();
                    children.add(0, treeStack.pop());
                }
                ParseTreeNode newNode = new ParseTreeNode(production.getLhs(), children);
                
                int topState = stateStack.peek();
                int nextState = parseTable.getGoto(topState, production.getLhs());
                
                if(nextState == -1) {
                    throw new IllegalStateException(
                        "GOTO error: no transition from state I" + topState
                        + " with non-terminal " + production.getLhs()
                    );
                }
                
                symbolStack.push(production.getLhs());
                stateStack.push(nextState);
                treeStack.push(newNode);
                
                importantActions.add("Reduce by " + formatProdution(production));
            }
        }
    }
    
    private String formatProdution(Production production) {
        return production.getLhs() + " -> " + String.join(" ", production.getRhs());
    }
    
    private String remainingInput(List<ParserToken> tokens, int start) {
        StringBuilder sb = new StringBuilder();
        
        for(int i = start; i < tokens.size(); i++) {
            sb.append(tokens.get(i).getTerminal());
            if(i < tokens.size() - 1) sb.append(" ");
        }
        return sb.toString();
    }
    
    public void printSteps() {
        System.out.println("=== PARSING STEPS ===");
        for (String step : steps) {
            System.out.println(step);
        }
    }
    
    private String getMostProbableExpectedToken(List<String> expectedTokens) {
        if (expectedTokens.isEmpty()) return "unknown";
        
        if (expectedTokens.contains(";")) return ";";
        
        if (expectedTokens.contains(")")) return ")";
        
        if (expectedTokens.contains(",")) return ",";
        
        return expectedTokens.get(0);
    }
    
    private List<String> getExpectedTokens(int state) {
        List<String> expected = new ArrayList<>();
        
        for (String terminal : grammar.getTerminals()) {
            if (parseTable.getAction(state, terminal) != 0) expected.add(terminal);
        }
        if (parseTable.getAction(state, "$") != 0 && !expected.contains("$"))
            expected.add("$");
        
        return expected;
    }
}
