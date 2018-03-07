package darch.cmd.types;

import darch.cmd.MapCommand;
import darch.map.MapController;
import darch.map.WallLocation;

public class AddDoorCommand implements MapCommand {

    final WallLocation wallLocation;

    public AddDoorCommand(WallLocation wallLocation) {
        this.wallLocation = wallLocation;
    }

    @Override
    public String asString() {
        return "d"+(wallLocation.getIndex()+1)+wallLocation.getDirection().name().subSequence(0,1);
    }

    @Override
    public void execute(MapController map) {
        map.addDoor(wallLocation);
    }

    @Override
    public MapCommand reverse() {
        throw new UnsupportedOperationException(); // TODO
    }

}
