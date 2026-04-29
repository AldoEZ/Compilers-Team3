package mx.unam.fi.compilers.g5.team03.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CompilerApp extends Application {
    @Override
    public void start(Stage stage) {
        MainView mainView = new MainView();
        new CompilerController(mainView);
        
        Scene scene = new Scene(mainView.getRoot(), 1250, 800);
        
        applyTheme(scene, true);
        mainView.getThemeButton().setSelected(true);
        mainView.getThemeButton().setText("Dark Mode");
        
        mainView.getThemeButton().setOnAction(e -> {
            boolean darkMode = mainView.getThemeButton().isSelected();
            applyTheme(scene, darkMode);
            mainView.getThemeButton().setText(darkMode ? "Dark Mode" : "Light Mode");
        });
        stage.setTitle("ParseFlow");
        stage.setScene(scene);
        stage.show();
    }
    
    private void applyTheme(Scene scene, boolean darkMode) {
        scene.getStylesheets().clear();
        
        String cssFile = darkMode
            ? "/mx/unam/fi/compilers/g5/team03/ui/dark-theme.css"
            : "/mx/unam/fi/compilers/g5/team03/ui/light-theme.css";
        
        scene.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
    }
}
