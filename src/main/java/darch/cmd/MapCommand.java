package darch.cmd;

import darch.map.MapController;

public interface MapCommand {

    String asString();

    void execute(MapController map);

    MapCommand reverse();

}
