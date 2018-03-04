package darch.map;

import javafx.geometry.Point2D;
import javafx.scene.transform.Affine;

import static darch.math.Matrices.multiply;

public class IsoMapNav extends AbstractMapNav {

    private static final double XSCALE = 0.75;
    private static final double YSCALE = 0.33;

    public IsoMapNav(Point2D origin, double gridSize) {
        super(origin, gridSize);
    }

    @Override
    public Point2D translateVector(Point2D v, double level) {
        return multiply(rotate45(v), gridSize * XSCALE, gridSize * YSCALE).add(0, level * gridSize * YSCALE);
    }

    private Point2D rotate45(Point2D vector) {
        return Affine.rotate(45, 0, 0).transform(vector);
    }

}