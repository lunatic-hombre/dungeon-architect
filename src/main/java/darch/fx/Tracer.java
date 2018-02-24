package darch.fx;

import javafx.geometry.Point2D;
import darch.map.CardinalPoint;
import darch.map.Direction;

public interface Tracer<E> {

    default Tracer<E> then(double distance, CardinalPoint cp) {
        return then(distance, cp.asDirection());
    }

    Tracer<E> then(double distance, Direction direction);

    Point2D last();

    E get();

}
