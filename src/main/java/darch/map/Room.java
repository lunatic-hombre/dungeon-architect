package darch.map;

import javafx.geometry.Point2D;

public interface Room {

    Room getParent();

    RelativeRoomLocation getLocation();

    int getLongitude();

    int getLatitude();

    int getLevel();

    int getDepth();

    int getLength();

    default double getHalfDepth() {
        return ((double) getDepth())/2d;
    }

    default double getHalfLength() {
        return ((double) getLength())/2d;
    }

    default Point2D getDimensions() {
        return new Point2D(getDepth(), getLength());
    }

}
