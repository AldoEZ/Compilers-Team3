package mx.unam.fi.compilers.g5.team03.parser;

import java.util.ArrayList;
import java.util.List;

public class ParseTreeNode {
    /**
     * symbol -> grammar symbol stored in the node
     * lexeme -> original lexeme, only used for terminals
     * children -> children nodes of current node
     */
    private final String symbol;
    private final String lexeme;
    private final List<ParseTreeNode> children;
    
    public ParseTreeNode(String symbol, String lexeme) {
        this.symbol = symbol;
        this.lexeme = lexeme;
        this.children = new ArrayList<>();
    }
    
    public ParseTreeNode(String symbol, List<ParseTreeNode> children) {
        this.symbol = symbol;
        this.lexeme = null;
        this.children = new ArrayList<>(children);
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getLexeme() {
        return lexeme;
    }
    
    public List<ParseTreeNode> getChildren() {
        return new ArrayList<>(children);
    }
    
    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        buildPrettyPrint(sb, 0);
        return sb.toString();
    }
    
    private void buildPrettyPrint(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) sb.append("  ");
        
        if (lexeme != null) {
            sb.append(symbol).append(" : ").append(lexeme).append("\n");
        } else sb.append(symbol).append("\n");
        
        for (ParseTreeNode child : children)
            child.buildPrettyPrint(sb, depth + 1);
    }
}
