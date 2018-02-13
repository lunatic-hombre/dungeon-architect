package mapgen;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mapgen.cmd.CommandInput;
import mapgen.map.IsometricMapCanvas;
import mapgen.map.MapCanvas;

public class Main extends Application {

    public static final int WIDTH = 800,  HEIGHT = 600;

    @Override
    public void start(Stage stage) {

        final BorderPane rootPane = new BorderPane();

        final Pane canvas = new Pane();
        rootPane.setCenter(canvas);

        final MapCanvas map = new IsometricMapCanvas(canvas, 30);
        final VBox bottomPane = new VBox();
        bottomPane.getChildren().add(new CommandInput(map, Throwable::printStackTrace));
        final Label help = new Label("Examples:" +
                "\n\t5x5\t\tnew 5x5 room" +
                "\n\t1x4n\t\tnew 1x4 hallway from North wall" +
                "\n\t1x4n2\tnew 1x4 hallway from 2nd grid of North wall" +
                "\n\t3x4s--\tnew 3x4 room at lower level from South wall" +
                "\n\ts4e\t\tadd stairway to 4th grid of East wall");
        help.setStyle("-fx-padding: 10px");
        bottomPane.getChildren().add(help);
        rootPane.setBottom(bottomPane);

        stage.setTitle("Isomap Generator");
        stage.setScene(new Scene(rootPane, WIDTH, HEIGHT+50));
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
