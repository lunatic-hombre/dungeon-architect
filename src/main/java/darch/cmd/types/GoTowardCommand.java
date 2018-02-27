package darch.cmd.types;

import darch.cmd.MapCommand;
import darch.map.MapCanvas;
import darch.map.WallLocation;

public class GoTowardCommand implements MapCommand {

    final WallLocation wallLocation;

    public GoTowardCommand(WallLocation wallLocation) {
        this.wallLocation = wallLocation;
    }

    @Override
    public String asString() {
        return (wallLocation.getIndex()+1)+wallLocation.getDirection().name().substring(0,1);
    }

    @Override
    public void execute(MapCanvas map) {
        map.go(wallLocation);
    }

    @Override
    public MapCommand reverse() {
        return new MapCommand() {
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
                return GoTowardCommand.this;
            }
        };
    }
}
