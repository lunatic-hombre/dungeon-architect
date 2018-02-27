package darch.map;

import javafx.geometry.Point2D;

public interface Room {

    Room getParent();

    RelativeRoomLocation getLocation();

    int getLongitude();

    int getLatitude();

    int getLevel();

    int getHorizontalScale();

    int getMeridianScale();

    default double getHalfHorizontal() {
        return ((double) getHorizontalScale())/2d;
    }

    default double getHalfMeridian() {
        return ((double) getMeridianScale())/2d;
    }

    default Point2D getDimensions() {
        return new Point2D(getHorizontalScale(), getMeridianScale());
    }

    default double getDimension(CardinalPoint direction) {
        return Math.abs(direction.getVector().dotProduct(getDimensions()));
    }

}
