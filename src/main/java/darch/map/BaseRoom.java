package darch.map;

public class BaseRoom implements Room {

    private final Room parent;
    private final RelativeRoomLocation location;
    private final int depth, length;

    public BaseRoom(int depth, int length) {
        this(null, null, depth, length);
    }

    public BaseRoom(Room parent, RelativeRoomLocation location, int depth, int length) {
        this.parent = parent;
        this.location = location;
        this.depth = depth;
        this.length = length;
    }

    @Override
    public Room getParent() {
        return parent;
    }

    @Override
    public RelativeRoomLocation getLocation() {
        return location;
    }

    @Override
    public int getLongitude() {
        if (parent == null)
            return 0;
        final int parentsLongitude = parent.getLongitude();
        return location.getDirection().isHorizontal()
                ? location.getDirection().getX() * (parentsLongitude + parent.getDepth()/2 + depth/2)
                : parentsLongitude;
    }

    @Override
    public int getLatitude() {
        if (parent == null)
            return 0;
        final int parentsLatitude = parent.getLatitude();
        return location.getDirection().isVertical()
                ? location.getDirection().getY() * (parentsLatitude + parent.getLength()/2 + length/2)
                : parentsLatitude;
    }

    @Override
    public int getLevel() {
        if (parent == null)
            return 0;
        final int parentsLevel = parent.getLevel();
        return location.getFloor().equals(RelativeLevel.SAME_LEVEL)
                ? parentsLevel
                : parentsLevel + location.getFloor().getScale();
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public int getLength() {
        return length;
    }

}
