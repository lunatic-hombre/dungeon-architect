package darch.map;

import javafx.geometry.Point2D;

public interface Room {

    Room getParent();

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

}
