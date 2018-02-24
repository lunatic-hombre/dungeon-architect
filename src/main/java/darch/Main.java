package darch;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import darch.cmd.CommandInputField;
import darch.cmd.ListMapCommandExecutor;
import darch.cmd.MapCommandExecutor;
import darch.map.IsoMap;
import darch.map.MapCanvas;

import java.io.File;
import java.io.IOException;


public class Main extends Application {

    public static final int WIDTH = 800,  HEIGHT = 600;

    File currentFile;

    @Override
    public void start(Stage stage) {

        final BorderPane rootPane = new BorderPane();

        final Pane canvas = new Pane();
        rootPane.setCenter(canvas);
        final MapCanvas map = new IsoMap(canvas, 30);
        final ListMapCommandExecutor commands = new ListMapCommandExecutor(map);

        final MenuBar menuBar = new MenuBar(
                menu("File",
                        menuItem("Open", new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), e -> loadFromFile(stage, commands)),
                        menuItem("Save", new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), e -> saveToFile(stage, commands)),
                        menuItem("Save As...", new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN), e -> saveAsFile(stage, commands)),
                        new MenuItem("Export..."),
                        new MenuItem("Print")),
                menu("Edit",
                        menuItem("Undo", new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN), e -> commands.undo()),
                        menuItem("Redo", new KeyCodeCombination(KeyCode.Z, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN), e -> commands.redo())),
                menu("Room",
                        menuItem("Back", new KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.SHORTCUT_DOWN), e -> commands.execute("back")),
                        menuItem("North", new KeyCodeCombination(KeyCode.UP, KeyCombination.SHORTCUT_DOWN), e -> commands.execute("n")),
                        menuItem("East", new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHORTCUT_DOWN), e -> commands.execute("e")),
                        menuItem("West", new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHORTCUT_DOWN), e -> commands.execute("w")),
                        menuItem("South", new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHORTCUT_DOWN), e -> commands.execute("s"))),
                menu("View",
                        new MenuItem("Zoom Out"), // TODO
                        new MenuItem("Zoom In"),
                        new MenuItem("Split Floors"),
                        new MenuItem("Show Labels"),
                        new MenuItem("Top-Down")));
        rootPane.setTop(menuBar);

        final VBox bottomPane = new VBox();
        bottomPane.getChildren().add(new CommandInputField(commands, Throwable::printStackTrace));
        final Label help = new Label("Examples:" +
                "\n\t5x5\t\tnew 5x5 room" +
                "\n\t1x4n\t\tnew 1x4 hallway from North wall" +
                "\n\t1x4n2\tnew 1x4 hallway from 2nd grid of North wall" +
                "\n\t3x4s--\tnew 3x4 room at lower level from South wall" +
                "\n\ts4e\t\tadd stairway to 4th grid of East wall");
        help.setStyle("-fx-padding: 10px");
        bottomPane.getChildren().add(help);
        rootPane.setBottom(bottomPane);

        stage.setTitle("Dungeon Workshop");
        final Scene scene = new Scene(rootPane, WIDTH, HEIGHT + 50);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.show();
    }

    private void loadFromFile(Stage stage, MapCommandExecutor commands) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("dungeon map files (*.dm)", "*.dm"));
        final File file = fileChooser.showOpenDialog(stage);
        try {
            commands.load(file);
            currentFile = file;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile(Stage stage, MapCommandExecutor commands) {
        if (currentFile == null)
            saveAsFile(stage, commands);
        else
            performSave(commands, currentFile);
    }

    private void saveAsFile(Stage stage, MapCommandExecutor commands) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("dungeon map files (*.dm)", "*.dm"));
        final File file = fileChooser.showSaveDialog(stage);
        performSave(commands, file);
    }

    private void performSave(MapCommandExecutor commands, File file) {
        try {
            if (file != null)
                commands.save(file);
            currentFile = file;
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
    }

    private Menu menu(String label, MenuItem... menuItems) {
        return new Menu(label, null, menuItems);
    }

    private MenuItem menuItem(String label, KeyCombination shortcut, EventHandler<ActionEvent> eventHandler) {
        final MenuItem menuItem = new MenuItem(label);
        menuItem.setAccelerator(shortcut);
        menuItem.setOnAction(eventHandler);
        return menuItem;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
