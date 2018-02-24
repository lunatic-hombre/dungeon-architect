package darch.cmd;

import darch.map.MapCanvas;

import java.io.*;
import java.util.LinkedList;

public class ListMapCommandExecutor implements MapCommandExecutor {

    private final MapCanvas map;
    private final Format<MapCommand> format;
    private final LinkedList<MapCommand> commands;

    public ListMapCommandExecutor(MapCanvas map) {
        this(map, MapCommands.getDefaultFormat(map), new LinkedList<>());
    }

    public ListMapCommandExecutor(MapCanvas map, Format<MapCommand> format, LinkedList<MapCommand> commands) {
        this.map = map;
        this.format = format;
        this.commands = commands;
    }

    @Override
    public void execute(String commandString) {
        execute(format.fromString(commandString));
    }

    @Override
    public void execute(MapCommand cmd) {
        cmd.execute(map);
        commands.add(cmd);
    }

    @Override
    public void undo() {
        execute(commands.getLast().reverse());
    }

    @Override
    public void redo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(File file) throws IOException {
        try (PrintStream out = new PrintStream(file)) {
            commands.forEach(cmd -> out.println(format.toString(cmd)));
        }
    }

    @Override
    public void load(File file) throws IOException {
        map.clear();
        commands.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
                execute(format.fromString(line));
        }
    }
}
