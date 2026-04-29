package mx.unam.fi.compilers.g5.team03.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class MainView {
    private final BorderPane root;
    
    private final CodeArea codeEditor;
    private final TextArea lexerOutput;
    private final TextArea parserOutput;
    private final TextArea semanticOutput;
    
    private final ParseTreePane parseTreePane;
    
    private final Button runButton;
    private final ToggleButton themeButton;
    
    private final ImageView companyLogo;
    private final ImageView productLogo;
    
    public MainView() {
        root = new BorderPane();
        
        codeEditor = new CodeArea();
        lexerOutput = new TextArea();
        parserOutput = new TextArea();
        semanticOutput = new TextArea();
        
        parseTreePane = new ParseTreePane();
        
        runButton = new Button("Run");
        themeButton = new ToggleButton("Light Mode");
        
        companyLogo = createLogoView(
            "/mx/unam/fi/compilers/g5/team03/ui/logos/rootnode-logo.png",
            42
        );
        
        productLogo = createLogoView(
            "/mx/unam/fi/compilers/g5/team03/ui/logos/parseflow-logo.png",
            42
        );
        
        buildLayout();
        configureComponents();
    }
    
    private void buildLayout() {
        root.getStyleClass().add("root-pane");
        
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(12));
        topBar.getStyleClass().add("top-bar");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox logosBox = new HBox(12);
        logosBox.getChildren().addAll(companyLogo, productLogo);
        
        topBar.getChildren().addAll(runButton, themeButton, spacer, logosBox);
        
        VBox editorPanel = new VBox(10);
        editorPanel.setPadding(new Insets(10));
        editorPanel.getStyleClass().add("panel");
        
        VBox.setVgrow(codeEditor, Priority.ALWAYS);
        editorPanel.getChildren().add(codeEditor);
        
        TabPane outputTabs = new TabPane();
        outputTabs.getStyleClass().add("output-tabs");
        
        Tab lexerTab = new Tab("Lexer Output", lexerOutput);
        Tab parserTab = new Tab("Parser Output", parserOutput);
        Tab semanticTab = new Tab("Semantic Output", semanticOutput);
        Tab parseTreeTab = new Tab("Parse Tree", parseTreePane);
        
        lexerTab.setClosable(false);
        parserTab.setClosable(false);
        semanticTab.setClosable(false);
        parseTreeTab.setClosable(false);
        
        outputTabs.getTabs().addAll(lexerTab, parserTab, semanticTab, parseTreeTab);
        
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(editorPanel, outputTabs);
        splitPane.setDividerPositions(0.38);
        splitPane.getStyleClass().add("main-split");
        
        root.setTop(topBar);
        root.setCenter(splitPane);
    }
    
    private void configureComponents() {
        codeEditor.setWrapText(false);
        codeEditor.getStyleClass().add("code-area");
        
        Label placeholder = new Label("Write your source code here...");
        placeholder.getStyleClass().add("code-placeholder");
        codeEditor.setPlaceholder(placeholder);
        
        lexerOutput.setEditable(false);
        parserOutput.setEditable(false);
        semanticOutput.setEditable(false);
        
        lexerOutput.setWrapText(true);
        parserOutput.setWrapText(true);
        semanticOutput.setWrapText(true);
        
        lexerOutput.getStyleClass().add("output-area");
        parserOutput.getStyleClass().add("output-area");
        semanticOutput.getStyleClass().add("output-area");
        
        runButton.getStyleClass().add("primary-button");
        themeButton.getStyleClass().add("theme-button");
    }
    
    public Parent getRoot() {
        return root;
    }
    
    public CodeArea getCodeEditor() {
        return codeEditor;
    }
    
    public TextArea getLexerOutput() {
        return lexerOutput;
    }
    
    public TextArea getParserOutput() {
        return parserOutput;
    }
    
    public TextArea getSemanticOutput() {
        return semanticOutput;
    }
    
    public ParseTreePane getParseTreePane() {
        return parseTreePane;
    }
    
    public Button getRunButton() {
        return runButton;
    }
    
    public ToggleButton getThemeButton() {
        return themeButton;
    }
    
    private ImageView createLogoView(String resourcePath, double fitHeight) {
        var stream = getClass().getResourceAsStream(resourcePath);
        
        if (stream == null)
            throw new IllegalArgumentException("Logo resource not found: " + resourcePath);
        
        Image image = new Image(stream);
        ImageView imageView = new ImageView(image);
        
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(fitHeight);
        imageView.setSmooth(true);
        
        return imageView;
    }
}
