package darch.map;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

public class OverheadMapNav extends AbstractMapNav {

    public OverheadMapNav(Point2D origin, double gridSize) {
        super(origin, gridSize);
    }

    @Override
    public Point2D translateVector(Point2D vector, double level) {
        return vector.multiply(gridSize);
    }

}
