package darch.map;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

public enum Direction {

    NORTH(0, -1),
    NORTH_EAST(1, -1),
    EAST(1, 0),
    SOUTH_EAST(1, 1),
    SOUTH(0, 1),
    SOUTH_WEST(-1, 1),
    WEST(-1, 0),
    NORTH_WEST(-1, -1),
    UP(0, 0, -1),
    DOWN(0, 0, 1);

    final int x, y, l;

    Direction(int x, int y) {
        this(x, y, 0);
    }

    Direction(int x, int y, int l) {
        this.x = x;
        this.y = y;
        this.l = l;
    }

    public boolean isX() {
        return y == 0 && l == 0;
    }

    public boolean isY() {
        return x == 0 && l == 0;
    }

    public boolean isElevation() {
        return l !=0;
    }

    public Point2D getVector() {
        return new Point2D(x, y);
    }

    public Point3D getVector3D() {
        return new Point3D(x, y, l);
    }

    public int getElevation() {
        return l;
    }

    public Direction rotate45() {
        if (this.isElevation())
            throw new IllegalStateException();
        return values()[(this.ordinal()+1)%8];
    }

    public Direction rotate90() {
        if (this.isElevation())
            throw new IllegalStateException();
        return values()[(this.ordinal()+2)%8];
    }

    public Direction rotate270() {
        if (this.isElevation())
            throw new IllegalStateException();
        return values()[(this.ordinal()+6)%8];
    }

    /**
     * Returns West if North or South, South if East or West
     */
    public Direction fallBack() {
        if (this.isElevation())
            throw new IllegalStateException();
        return isY() ? WEST : SOUTH;
    }

    public Direction reverse() {
        if (this.isElevation())
            return this.equals(UP) ? DOWN : UP;
        return values()[(this.ordinal()+4)%8];
    }

}
