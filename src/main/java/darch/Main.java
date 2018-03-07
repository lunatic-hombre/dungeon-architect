package darch;

import darch.cmd.CommandInputField;
import darch.cmd.ListMapCommandExecutor;
import darch.cmd.MapCommandExecutor;
import darch.map.*;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;


public class Main extends Application {

    private static final int WIDTH = 800,  HEIGHT = 600, GRID_SIZE = 30;
    private static Point2D CENTER = new Point2D(WIDTH/2, HEIGHT/2-50);

    File currentFile;

    @Override
    public void start(Stage stage) {

        final BorderPane rootPane = new BorderPane();

        final Pane isoCanvas = new Pane(), overheadCanvas = new Pane();
        rootPane.setCenter(isoCanvas);
        final MapController map = new MultiViewMapController(
                new MapViewImpl(isoCanvas, new IsoMapNav(CENTER, GRID_SIZE)),
                new MapViewImpl(overheadCanvas, new OverheadMapNav(CENTER, GRID_SIZE))
        );
        final ListMapCommandExecutor commands = new ListMapCommandExecutor(map);

        final MenuBar menuBar = new MenuBar(
                menu("File",
                        menuItem("Open", new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), e -> loadFromFile(stage, commands)),
                        menuItem("Save", new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), e -> saveToFile(stage, commands)),
                        menuItem("Save As...", new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN), e -> saveAsFile(stage, commands)),
                        menuItem("Export...", new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN), e -> export(stage, (Parent) rootPane.getCenter())),
                        menuItem("Print", new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN), e -> print(stage, (Parent) rootPane.getCenter()))),
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
                        menuItem("Zoom Out", new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN), e -> { isoCanvas.getTransforms().add(new Scale(0.75, 0.75)); map.centerViewPort(); }), // TODO
                        menuItem("Zoom In", new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN), e -> { isoCanvas.getTransforms().add(new Scale(1.25, 1.25)); map.centerViewPort(); }),
                        checkboxMenuItem("Overhead", new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), (e, checked) -> rootPane.setCenter(checked ? overheadCanvas : isoCanvas) )));
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

        stage.setTitle("Dungeon Architect");
        final Scene scene = new Scene(rootPane, WIDTH, HEIGHT + 50);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.show();
    }

    private void print(Stage stage, Parent node) {
        final PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null)
            throw new RuntimeException("Printing not available!");
        boolean proceed = job.showPageSetupDialog(stage);
        if (!proceed)
            return;
        proceed = job.showPrintDialog(stage);
        if (!proceed)
            return;

        try {
            final File temp = File.createTempFile("test", "png");
            currentFile.deleteOnExit();
            saveToFile(node, temp);

            final boolean success = job.printPage(node);
            if (success) {
                job.endJob();
            }
            node.setClip(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void export(Stage stage, Parent node) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("pixel nemmrrr graphics (*.png)", "*.png"));
        final File file = fileChooser.showSaveDialog(stage);
        saveToFile(node, file);
    }

    private void saveToFile(Parent node, File file) {
        if (node.getChildrenUnmodifiable().isEmpty())
            return;
        final SnapshotParameters snapshotParameters = new SnapshotParameters();
        final Rectangle2D viewport = getVisibleArea(node);
        snapshotParameters.setViewport(viewport);
        final WritableImage image = node.snapshot(snapshotParameters, null);
        try {
            if (!ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file))
                System.err.println("Image didn't write?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO limited by viewport?
    private Rectangle2D getVisibleArea(Parent node) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = 0, maxY = 0;
        for (Node child : node.getChildrenUnmodifiable()) {
            minX = Math.min(minX, child.getBoundsInParent().getMinX());
            maxX = Math.max(maxX, child.getBoundsInParent().getMaxX());
            minY = Math.min(minY, child.getBoundsInParent().getMinY());
            maxY = Math.max(maxY, child.getBoundsInParent().getMaxY());
        }
        return new Rectangle2D(
                minX + node.getBoundsInParent().getMinX(),
                minY + node.getBoundsInParent().getMinY(),
                (maxX - minX),
                (maxY - minY));
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

    private CheckMenuItem checkboxMenuItem(String label, KeyCombination shortcut, BiConsumer<ActionEvent, Boolean> eventHandler) {
        final CheckMenuItem menuItem = new CheckMenuItem(label);
        menuItem.setAccelerator(shortcut);
        menuItem.setOnAction(e -> eventHandler.accept(e, menuItem.isSelected()));
        return menuItem;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
