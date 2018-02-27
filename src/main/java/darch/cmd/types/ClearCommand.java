package darch.cmd.types;

import darch.cmd.MapCommand;
import darch.map.MapCanvas;

public class ClearCommand implements MapCommand {

    @Override
    public String asString() {
        return "clear";
    }

    @Override
    public void execute(MapCanvas map) {
        map.clear();
    }

    @Override
    public MapCommand reverse() {
        throw new UnsupportedOperationException(); // TODO
    }

}
