package darch.map;

import javafx.geometry.Point2D;

import java.util.stream.Stream;

public enum CardinalPoint {

    NORTH(0, -1, false),
    EAST(1, 0, true),
    SOUTH(0, 1, false),
    WEST(-1, 0, true);

    double[] matrix;
    boolean horizontal;

    CardinalPoint(double x, double y, boolean horizontal) {
        this.matrix = new double[] {x, y};
        this.horizontal = horizontal;
    }

    public double[] getMatrix() {
        return matrix;
    }

    public Point2D getVector() {
        return new Point2D(matrix[0], matrix[1]);
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public boolean isVertical() {
        return !horizontal;
    }

    public CardinalPoint rotate90() {
        return values()[(ordinal()+1)%4];
    }

    public CardinalPoint reverse() {
        return values()[(ordinal()+2)%4];
    }

    public Direction asDirection() {
        return Direction.valueOf(name());
    }

    public static Point2D getVector(CardinalPoint d1, CardinalPoint d2) {
        return d1.getVector().add(d2.getVector());
    }

    public static CardinalPoint byChar(char c) {
        final char ch = Character.toUpperCase(c);
        return Stream.of(values())
                .filter(direction -> direction.name().charAt(0) == ch)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown direction "+c));
    }

}
