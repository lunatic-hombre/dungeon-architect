package darch.map;

import darch.math.Matrices;
import javafx.geometry.Point2D;

public class IsoMapNav extends AbstractMapNav {

    protected static final double XSCALE = 2;
    protected static final double YSCALE = 5;

    // TODO midpoint doesn't belong here
    public IsoMapNav(double gridSize) {
        super(gridSize);
    }

    // TODO incorporate gridsize into getVector method
    @Override
    public Point2D getVector(Direction direction) {
        return direction.isElevation()
                ? new Point2D(0, direction.getElevation() * MapConstants.ROOM_HEIGHT / YSCALE).multiply(gridSize)
                : Matrices.multiply(direction.rotate45().getVector(), 1 / XSCALE, 1 / YSCALE).multiply(gridSize);
    }

}