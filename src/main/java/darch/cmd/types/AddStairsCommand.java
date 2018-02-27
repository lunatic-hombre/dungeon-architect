package darch.cmd.types;

import darch.cmd.MapCommand;
import darch.map.MapCanvas;
import darch.map.WallLocation;

public class AddStairsCommand implements MapCommand {

    final WallLocation wallLocation;

    public AddStairsCommand(WallLocation wallLocation) {
        this.wallLocation = wallLocation;
    }

    @Override
    public String asString() {
        return "s"+(wallLocation.getIndex()+1)+wallLocation.getDirection().name().subSequence(0,1);
    }

    @Override
    public void execute(MapCanvas map) {
        map.addStairs(wallLocation);
    }

    @Override
    public MapCommand reverse() {
        throw new UnsupportedOperationException(); // TODO
    }

}
