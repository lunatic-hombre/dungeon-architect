package darch.cmd;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import darch.err.ErrorReporter;


public class CommandInputField extends TextField {

    private final MapCommandExecutor commandExecutor;
    private final ErrorReporter errorReporter;

    public CommandInputField(MapCommandExecutor commandExecutor,
                             ErrorReporter errorReporter) {
        this.commandExecutor = commandExecutor;
        this.errorReporter = errorReporter;
        this.setOnKeyPressed(this::onKeyPress);
    }

    private void onKeyPress(KeyEvent keyEvent) {
        final KeyCode code = keyEvent.getCode();
        // focus on the text input will override some shortcuts from the main app, thus they must be repeated here.
        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
            if (code.equals(KeyCode.Z))
                commandExecutor.undo();
        }
        else if (code.equals(KeyCode.ENTER)) {
            try {
                interpretCommand(this.getText().trim());
            } catch (Exception e) {
                errorReporter.reportError(e);
            }
            this.clear();
        }
    }

    private void interpretCommand(String text) {
        try {
            commandExecutor.execute(text);
        } catch (Exception e) {
            errorReporter.reportError(e);
        }
    }

}
