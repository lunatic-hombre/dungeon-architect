package sample.cmd;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import sample.err.ErrorReporter;
import sample.map.BaseRoom;
import sample.map.MapCanvas;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommandInput extends TextField {

    private static final Pattern ROOM_CMD_PATTERN = Pattern.compile("r\\s+(\\d+)\\s+(\\d+)");

    private final MapCanvas mapCanvas;
    private final ErrorReporter errorReporter;

    public CommandInput(MapCanvas mapCanvas, ErrorReporter errorReporter) {
        this.mapCanvas = mapCanvas;
        this.errorReporter = errorReporter;
        this.setOnKeyPressed(this::onKeyPress);
    }

    private void onKeyPress(KeyEvent keyEvent) {
        final KeyCode code = keyEvent.getCode();
        if (code.equals(KeyCode.ENTER)) {
            try {
                interpretCommand(this.getText().trim());
            } catch (Exception e) {
                errorReporter.reportError(e);
            }
            this.clear();
        }
    }

    private void interpretCommand(String text) {
        Matcher matcher;
        if ((matcher = ROOM_CMD_PATTERN.matcher(text)).matches())
            mapCanvas.drawRoom(null, new BaseRoom(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
        else
            throw new UnsupportedOperationException("Unknown command \""+text+"\".");
    }

}
