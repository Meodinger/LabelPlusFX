package info.meodinger.LabelPlusFX;

import info.meodinger.LabelPlusFX.Util.CDialog;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;


public class LabelPlusFX extends Application {

    static {
        I18N.init();
    }

    private State state;

    @Override
    public void start(Stage primaryStage) throws Exception{
        state = new State(this, primaryStage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Window.fxml"));
        loader.setControllerFactory(Controller -> new Controller(state));
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);

        // Global event catch, prevent mnemonic parsing
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(event.isAltDown()) event.consume();
        });

        primaryStage.setOnCloseRequest(e -> state.getControllerAccessor().close());
        primaryStage.setTitle(I18N.WINDOW_TITLE);
        primaryStage.getIcons().add(Resources.ICON);
        primaryStage.setScene(scene);
        primaryStage.show();

        CDialog.initOwner(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
