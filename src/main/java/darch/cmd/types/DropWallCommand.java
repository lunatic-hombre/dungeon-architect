package darch.cmd.types;

import darch.cmd.MapCommand;
import darch.map.CardinalPoint;
import darch.map.MapCanvas;

public class DropWallCommand implements MapCommand {

    final CardinalPoint direction;

    public DropWallCommand(CardinalPoint direction) {
        this.direction = direction;
    }

    @Override
    public String asString() {
        return "drop" + direction.name().charAt(0);
    }

    @Override
    public void execute(MapCanvas map) {
        map.dropWall(direction);
    }

    @Override
    public MapCommand reverse() {
        throw new UnsupportedOperationException();
    }

}
