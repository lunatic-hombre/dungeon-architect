package darch.map;

public class BasicRoom implements Room {

    private final Room parent;
    private final RelativeRoomLocation location;
    private final int horizontal, meridian;

    public BasicRoom(int horizontal, int meridian) {
        this(null, null, horizontal, meridian);
    }

    public BasicRoom(Room parent, RelativeRoomLocation location, int horizontal, int meridian) {
        this.parent = parent;
        this.location = location;
        this.horizontal = horizontal;
        this.meridian = meridian;
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
                ? location.getDirection().getX() * (parentsLongitude + parent.getHorizontalScale()/2 + horizontal /2)
                : parentsLongitude;
    }

    @Override
    public int getLatitude() {
        if (parent == null)
            return 0;
        final int parentsLatitude = parent.getLatitude();
        return location.getDirection().isVertical()
                ? location.getDirection().getY() * (parentsLatitude + parent.getMeridianScale()/2 + meridian /2)
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
    public int getHorizontalScale() {
        return horizontal;
    }

    @Override
    public int getMeridianScale() {
        return meridian;
    }

}
