package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import sample.cmd.CommandInput;
import sample.map.IsometricMapCanvas;
import sample.map.MapCanvas;

public class Main extends Application {

    public static final int WIDTH = 500,  HEIGHT = 375;

    @Override
    public void start(Stage stage) {

        final BorderPane pane = new BorderPane();

        final MapCanvas map = new IsometricMapCanvas(WIDTH, HEIGHT, 15);
        pane.setCenter(map.getCanvas());
        pane.setBottom(new CommandInput(map, Throwable::printStackTrace));

        stage.setTitle("Isomap Generator");
        stage.setScene(new Scene(pane, WIDTH, HEIGHT+50));
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
