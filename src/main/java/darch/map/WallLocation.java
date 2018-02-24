package darch.map;

public class WallLocation {

    private final CardinalPoint direction;
    private final int index;

    public WallLocation(CardinalPoint direction, int index) {
        this.direction = direction;
        this.index = index;
    }

    public CardinalPoint getDirection() {
        return direction;
    }

    public int getIndex() {
        return index;
    }

}
