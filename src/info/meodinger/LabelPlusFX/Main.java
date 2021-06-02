package info.meodinger.LabelPlusFX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    static {
        I18N.init();
    }

    private Config config;

    @Override
    public void start(Stage primaryStage) throws Exception{
        config = new Config(primaryStage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Window.fxml"));
        loader.setControllerFactory(Controller -> new Controller(config));
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);

        // Global event catch
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(event.isAltDown()) event.consume();
        });

        primaryStage.setOnCloseRequest(e -> config.getControllerAccessor().close());
        primaryStage.setTitle(I18N.WINDOW_TITLE);
        primaryStage.getIcons().add(new Image(Resources.ICON));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
