package darch.cmd.types;

import darch.cmd.MapCommand;
import darch.map.MapController;

public class ClearCommand implements MapCommand {

    @Override
    public String asString() {
        return "clear";
    }

    @Override
    public void execute(MapController map) {
        map.clear();
    }

    @Override
    public MapCommand reverse() {
        throw new UnsupportedOperationException(); // TODO
    }

}
