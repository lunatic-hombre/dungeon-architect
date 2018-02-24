package darch.cmd;

import darch.map.MapCanvas;

public interface MapCommand {

    String asString();

    void execute(MapCanvas map);

    MapCommand reverse();

}
