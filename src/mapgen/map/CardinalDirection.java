package mapgen.map;

import javafx.geometry.Point2D;

import java.util.stream.Stream;

public enum CardinalDirection {

    NORTH(0, -1, false),
    EAST(1, 0, true),
    SOUTH(0, 1, false),
    WEST(-1, 0, true);

    double[] matrix;
    boolean horizontal;

    CardinalDirection(double x, double y, boolean horizontal) {
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

    public static Point2D getVector(CardinalDirection d1, CardinalDirection d2) {
        return d1.getVector().add(d2.getVector());
    }

    public static CardinalDirection byChar(char c) {
        final char ch = Character.toUpperCase(c);
        return Stream.of(values())
                .filter(direction -> direction.name().charAt(0) == ch)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown direction "+c));
    }

}
