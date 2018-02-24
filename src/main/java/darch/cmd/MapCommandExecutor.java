package darch.cmd;

import java.io.File;
import java.io.IOException;

public interface MapCommandExecutor {

    void execute(String commandString);

    void execute(MapCommand mapCommand);

    void undo();

    void redo();

    void save(File file) throws IOException;

    void load(File file) throws IOException;

}
