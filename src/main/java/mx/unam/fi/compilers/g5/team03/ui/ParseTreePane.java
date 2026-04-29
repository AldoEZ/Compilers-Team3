package mx.unam.fi.compilers.g5.team03.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import mx.unam.fi.compilers.g5.team03.parser.ParseTreeNode;

public class ParseTreePane extends StackPane {
    private static final double NODE_WIDTH = 130;
    private static final double NODE_HEIGHT = 36;
    private static final double H_GAP = 24;
    private static final double V_GAP = 70;
    private static final double TOP_MARGIN = 30;
    private static final double SIDE_MARGIN = 30;
    
    private final ScrollPane scrollPane;
    private final Pane canvas;
    private final Label placeholder;
    
    public ParseTreePane() {
        getStyleClass().add("tree-pane");
        
        canvas = new Pane();
        
        scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(false);
        scrollPane.setPannable(true);
        
        scrollPane.getStyleClass().add("tree-scroll");
        canvas.getStyleClass().add("tree-canvas");
        
        placeholder = new Label("Parse tree will be rendered here");
        placeholder.getStyleClass().add("tree-placeholder");
        
        setAlignment(Pos.CENTER);
        getChildren().add(placeholder);
    }
    
    public void setTree(ParseTreeNode root) {
        if(root == null) {
            showPlaceholder("No parse tree generated.");
            return;
        }
        
        getChildren().clear();
        getChildren().add(scrollPane);
        
        canvas.getChildren().clear();
        
        double treeWidth = computeSubtreeWidth(root);
        double treeHeight = computeTreeHeight(root);
        
        double rootCenterX = SIDE_MARGIN + (treeWidth / 2.0);
        
        layoutTree(root, rootCenterX, TOP_MARGIN);
        
        canvas.setPrefWidth(Math.max(treeWidth + 2 * SIDE_MARGIN, 900));
        canvas.setPrefHeight(Math.max(treeHeight + TOP_MARGIN + 60, 600));
    }
    
    public void setTreeText(String text) {
        if(text == null || text.isBlank()) {
            showPlaceholder("No parse tree generated.");
        } else showPlaceholder(text);
    }
    
    private void showPlaceholder(String text) {
        getChildren().clear();
        placeholder.setText(text);
        getChildren().add(placeholder);
    }
    
    private double computeSubtreeWidth(ParseTreeNode node) {
        if (node.getChildren().isEmpty()) return NODE_WIDTH;
        
        double total = 0;
        for(int i = 0; i < node.getChildren().size(); i++) {
            total += computeSubtreeWidth(node.getChildren().get(i));
            if (i < node.getChildren().size() - 1)
                total += H_GAP;
        }
        return Math.max(NODE_WIDTH, total);
    }
    
    private double computeTreeHeight(ParseTreeNode node) {
        return computeDepth(node) * (NODE_HEIGHT + V_GAP);
    }
    
    private int computeDepth(ParseTreeNode node) {
        if(node.getChildren().isEmpty())
            return 1;
        
        int maxDepth = 0;
        for(ParseTreeNode child : node.getChildren())
            maxDepth = Math.max(maxDepth, computeDepth(child));
        
        return 1 + maxDepth;
    }
    
    private void layoutTree(ParseTreeNode node, double centerX, double topY) {
        StackPane nodeBox = createNodeBox(getNodeLabel(node));
        nodeBox.setLayoutX(centerX - NODE_WIDTH / 2.0);
        nodeBox.setLayoutY(topY);
        canvas.getChildren().add(nodeBox);
        
        if(node.getChildren().isEmpty()) return;
        
        double childrenWidth = 0;
        for(int i = 0; i < node.getChildren().size(); i++) {
            childrenWidth += computeSubtreeWidth(node.getChildren().get(i));
            if(i < node.getChildren().size() - 1)
                childrenWidth += H_GAP;
        }
        
        double currentX = centerX - childrenWidth / 2.0;
        double childY = topY + NODE_HEIGHT + V_GAP;
        
        for(ParseTreeNode child : node.getChildren()) {
            double childWidth = computeSubtreeWidth(child);
            double childCenterX = currentX + childWidth / 2.0;
            
            Line edge = new Line(
                centerX,
                topY + NODE_HEIGHT,
                childCenterX,
                childY
            );
            edge.getStyleClass().add("tree-edge");
            canvas.getChildren().add(edge);
            
            layoutTree(child, childCenterX, childY);
            
            currentX += childWidth + H_GAP;
        }
    }
    
    private StackPane createNodeBox(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("tree-node-label");
        label.setWrapText(true);
        label.setMaxWidth(NODE_WIDTH - 12);
        label.setAlignment(Pos.CENTER);
        
        StackPane box = new StackPane(label);
        box.getStyleClass().add("tree-node");
        box.setPrefSize(NODE_WIDTH, NODE_HEIGHT);
        box.setMinSize(NODE_WIDTH, NODE_HEIGHT);
        box.setMaxSize(NODE_WIDTH, NODE_HEIGHT);
        
        return box;
    }
    
    private String getNodeLabel(ParseTreeNode node) {
        String symbol = node.getSymbol();
        String lexeme = node.getLexeme();
        
        if (lexeme == null || lexeme.isBlank()) return symbol;
        
        if (symbol.equals(lexeme)) return symbol;
        
        return symbol + " : " + lexeme;
    }
}
