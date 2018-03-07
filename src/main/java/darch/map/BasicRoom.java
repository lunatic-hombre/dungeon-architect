package darch.map;

public class BasicRoom implements Room {

    private final Room parent;
    private final RelativeRoomLocation location;
    private final double horizontal, meridian;

    public BasicRoom(int horizontal, int meridian) {
        this(null, null, horizontal, meridian);
    }

    public BasicRoom(Room parent, RelativeRoomLocation location, double horizontal, double meridian) {
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
    public double getLongitude() {
        if (parent == null)
            return 0;
        return parent.getLongitude()
                + location.getDirection().getX() * (parent.getHalfHorizontal() + getHalfHorizontal())
                + location.getDirection().fallback().reverse().getX() * (location.getParentIndex() - location.getChildIndex() - parent.getHalfHorizontal() + getHalfHorizontal());
    }

    @Override
    public double getLatitude() {
        if (parent == null)
            return 0;
        return parent.getLatitude()
                + location.getDirection().getY() * (parent.getHalfMeridian() + getHalfMeridian())
                + location.getDirection().fallback().reverse().getY() * (location.getParentIndex() - location.getChildIndex() - parent.getHalfMeridian() + getHalfMeridian());
    }

    @Override
    public int getLevel() {
        if (parent == null)
            return 0;
        return parent.getLevel() + location.getFloor().getScale();
    }

    @Override
    public double getHorizontal() {
        return horizontal;
    }

    @Override
    public double getMeridian() {
        return meridian;
    }

}
