package darch.map;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

public interface Room {

    /**
     * Get the room from which this one branches.  Returns null if no parent.
     */
    Room getParent();

    /**
     * Get the distance and direction this room has branched from its parent.  Returns null if no parent.
     */
    RelativeRoomLocation getLocation();

    int getLongitude();

    int getLatitude();

    int getLevel();

    int getHorizontal();

    int getMeridian();

    default double getHalfHorizontal() {
        return ((double) getHorizontal())/2d;
    }

    default double getHalfMeridian() {
        return ((double) getMeridian())/2d;
    }

    default Point2D getDimensions() {
        return new Point2D(getHorizontal(), getMeridian());
    }

    default double getDimension(CardinalPoint direction) {
        return Math.abs(direction.getVector().dotProduct(getDimensions()));
    }

    default Point2D getMidpoint() {
        return new Point2D(getLongitude(), getLatitude());
    }

    default Point3D getMidpoint3D() {
        return new Point3D(getLongitude(), getLatitude(), getLevel());
    }

    default double getStartX() {
        return getLongitude() - getHalfHorizontal();
    }

    default double getEndX() {
        return getLongitude() + getHalfHorizontal();
    }

    default Range getRangeX() {
        return Range.range(getStartX(), getEndX());
    }

    default double getStartY() {
        return getLatitude() - getHalfMeridian();
    }

    default double getEndY() {
        return getLatitude() + getHalfMeridian();
    }

    default Range getRangeY() {
        return Range.range(getStartY(), getEndY());
    }

    default boolean isAdjacent(Room other) {
        return (getStartY() == other.getEndY() || getEndY() == other.getStartY()) && getRangeX().overlaps(other.getRangeX())
            || (getStartX() == other.getEndX() || getEndX() == other.getStartX()) && getRangeY().overlaps(other.getRangeY());
    }

}
