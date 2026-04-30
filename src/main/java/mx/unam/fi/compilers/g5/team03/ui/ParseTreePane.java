package mx.unam.fi.compilers.g5.team03.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
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
    
    private static final double MIN_ZOOM = 0.3;
    private static final double MAX_ZOOM = 3.0;
    private static final double ZOOM_STEP = 1.1;
    
    private final ScrollPane scrollPane;
    private final Pane canvas;
    private final Label placeholder;
    
    private final Button zoomInButton;
    private final Button zoomOutButton;
    private final Button resetZoomButton;
    
    private final StackPane contentLayer;
    
    private double zoomFactor;
    
    public ParseTreePane() {
        getStyleClass().add("tree-pane");
        
        canvas = new Pane();
        canvas.getStyleClass().add("tree-canvas");
        
        scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(false);
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add("tree-scroll");
        
        placeholder = new Label("Parse tree will be rendered here");
        placeholder.getStyleClass().add("tree-placeholder");
        
        zoomInButton = new Button("+");
        zoomOutButton = new Button("-");
        resetZoomButton = new Button("Reset");
        
        zoomInButton.getStyleClass().add("theme-button");
        zoomOutButton.getStyleClass().add("theme-button");
        resetZoomButton.getStyleClass().add("theme-button");
        
        HBox controlsBox = new HBox(8, zoomOutButton, zoomInButton, resetZoomButton);
        controlsBox.setPadding(new Insets(12));
        controlsBox.setAlignment(Pos.TOP_RIGHT);
        controlsBox.setMouseTransparent(false);
        controlsBox.setPickOnBounds(false);
        controlsBox.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
        
        contentLayer = new StackPane(scrollPane, controlsBox);
        StackPane.setAlignment(scrollPane, Pos.CENTER);
        StackPane.setAlignment(controlsBox, Pos.TOP_RIGHT);
        
        zoomFactor = 1.0;
        
        connectEvents();
        
        setAlignment(Pos.CENTER);
        getChildren().addAll(contentLayer, placeholder);
        
        showPlaceholder("Parse tree will be rendered here");
    }
    
    public void setTree(ParseTreeNode root) {
        if(root == null) {
            showPlaceholder("No parse tree generated.");
            return;
        }
        canvas.getChildren().clear();
        
        double treeWidth = computeSubtreeWidth(root);
        double treeHeight = computeTreeHeight(root);
        
        double rootCenterX = SIDE_MARGIN + (treeWidth / 2.0);
        
        layoutTree(root, rootCenterX, TOP_MARGIN);
        
        canvas.setPrefWidth(Math.max(treeWidth + 2 * SIDE_MARGIN, 900));
        canvas.setPrefHeight(Math.max(treeHeight + TOP_MARGIN + 60, 600));
        
        applyZoom();
        showTree();
    }
    
    public void setTreeText(String text) {
        if(text == null || text.isBlank()) {
            showPlaceholder("No parse tree generated.");
        } else showPlaceholder(text);
    }
    
    public void zoomIn() {
        zoomFactor = Math.min(zoomFactor * ZOOM_STEP, MAX_ZOOM);
        applyZoom();
    }
    
    public void zoomOut() {
        zoomFactor = Math.max(zoomFactor / ZOOM_STEP, MIN_ZOOM);
        applyZoom();
    }
    
    public void resetZoom() {
        zoomFactor = 1.0;
        applyZoom();
    }
    
    public double getZoomFactor() {
        return zoomFactor;
    }
    
    private void connectEvents() {
        zoomInButton.setOnAction(e -> zoomIn());
        zoomOutButton.setOnAction(e -> zoomOut());
        resetZoomButton.setOnAction(e -> resetZoom());
        
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if(event.isControlDown()) {
                if(event.getDeltaY() > 0) {
                    zoomIn();
                } else if (event.getDeltaY() < 0) zoomOut();
                event.consume();
            }
        });
    }
    
    private void applyZoom() {
        canvas.setScaleX(zoomFactor);
        canvas.setScaleY(zoomFactor);
    }
    
    private void showPlaceholder(String text) {
        placeholder.setText(text);
        placeholder.setVisible(true);
        placeholder.setManaged(true);
        
        contentLayer.setVisible(false);
        contentLayer.setManaged(false);
    }
    
    private void showTree() {
        placeholder.setVisible(false);
        placeholder.setManaged(false);
        
        contentLayer.setVisible(true);
        contentLayer.setManaged(true);
    }
    
    private double computeSubtreeWidth(ParseTreeNode node) {
        if(node.getChildren().isEmpty()) return NODE_WIDTH;
        
        double total = 0;
        for(int i = 0; i < node.getChildren().size(); i++) {
            total += computeSubtreeWidth(node.getChildren().get(i));
            if(i < node.getChildren().size() - 1) 
                total += H_GAP;
        }
        return Math.max(NODE_WIDTH, total);
    }
    
    private double computeTreeHeight(ParseTreeNode node) {
        return computeDepth(node) * (NODE_HEIGHT + V_GAP);
    }
    
    private int computeDepth(ParseTreeNode node) {
        if(node.getChildren().isEmpty()) return 1;
        
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
        
        if(lexeme == null || lexeme.isBlank()) return symbol;
        
        if(symbol.equals(lexeme)) return symbol;
        
        return symbol + " : " + lexeme;
    }
}
