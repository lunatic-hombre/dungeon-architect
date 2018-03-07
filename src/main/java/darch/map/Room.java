package darch.map;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import static darch.math.Matrices.multiply;

public interface Room {

    /**
     * Get the room from which this one branches.  Returns null if no parent.
     */
    Room getParent();

    /**
     * Get the distance and direction this room has branched from its parent.  Returns null if no parent.
     */
    RelativeRoomLocation getLocation();

    double getLongitude();

    double getLatitude();

    int getLevel();

    double getHorizontal();

    double getMeridian();

    default double getHalfHorizontal() {
        return getHorizontal()/2d;
    }

    default double getHalfMeridian() {
        return getMeridian()/2d;
    }

    default Point2D getDimensions() {
        return new Point2D(getHorizontal(), getMeridian());
    }

    default Point2D getHalfDimensions() {
        return new Point2D(getHalfHorizontal(), getHalfMeridian());
    }

    default double getDimension(CardinalPoint direction) {
        return Math.abs(direction.getVector().dotProduct(getDimensions()));
    }

    default Point2D getMidpoint() {
        return new Point2D(getLongitude(), getLatitude());
    }

    default Point2D getMidWall(CardinalPoint direction) {
        return getMidpoint().add(multiply(direction.getVector(), getHalfDimensions()));
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
        return Range.exclusive(getStartX(), getEndX());
    }

    default double getStartY() {
        return getLatitude() - getHalfMeridian();
    }

    default double getEndY() {
        return getLatitude() + getHalfMeridian();
    }

    default Range getRangeY() {
        return Range.exclusive(getStartY(), getEndY());
    }

    default double getX(CardinalPoint direction) {
        return getLongitude() + getHalfHorizontal() * direction.getX();
    }

    default double getY(CardinalPoint direction) {
        return getLatitude() + getHalfMeridian() * direction.getY();
    }

    default Point2D getOffset(WallLocation wallLocation) {
        return getOffset(wallLocation.getDirection(), wallLocation.getIndex());
    }

    default Point2D getOffset(CardinalPoint direction, int offset) {
        final CardinalPoint back = direction.fallback();
        return getMidWall(direction)
                .add(multiply(getHalfDimensions(), back.getVector()))
                .add(back.reverse().getVector().multiply(offset));
    }

    default boolean isAdjacent(Room other) {
        return (getStartY() == other.getEndY() || getEndY() == other.getStartY()) && getRangeX().overlaps(other.getRangeX())
            || (getStartX() == other.getEndX() || getEndX() == other.getStartX()) && getRangeY().overlaps(other.getRangeY());
    }

    default boolean isAdjacent(Room other, CardinalPoint direction) {
        return direction.isHorizontal()
                ? getX(direction) == other.getX(direction.reverse()) && getRangeY().overlaps(other.getRangeY())
                : getY(direction) == other.getY(direction.reverse()) && getRangeX().overlaps(other.getRangeX());
    }


}
