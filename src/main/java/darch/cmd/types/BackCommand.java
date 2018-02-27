package darch.cmd.types;

import darch.cmd.MapCommand;
import darch.map.MapCanvas;

public class BackCommand implements MapCommand {

    @Override
    public String asString() {
        return "back";
    }

    @Override
    public void execute(MapCanvas map) {
        map.back();
    }

    @Override
    public MapCommand reverse() {
        throw new UnsupportedOperationException();
    }

}
