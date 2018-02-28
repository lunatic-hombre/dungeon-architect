package darch.map;

import javafx.geometry.Point2D;

public class OverheadMapNav extends AbstractMapNav {

    public OverheadMapNav(double gridSize) {
        super(gridSize);
    }

    @Override
    public Point2D getVector(Direction direction) {
        return direction.getVector();
    }

}
