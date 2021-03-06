package darch.map;

import darch.fx.Tracer;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.shape.Polygon;

public interface MapNav {

    Point2D translateGridPoint(Point2D point, double level);

    Point2D translateVector(Point2D vector, double level);

    Tracer<Point2D> points(Point2D start);

    Tracer<Polygon> poly(Point2D start, String... styleClasses);

    default Point2D relativePoint(Point2D p, double distance, CardinalPoint direction) {
        return relativePoint(p, distance, direction.asDirection());
    }

    Point2D relativePoint(Point2D p, double distance, Direction direction);

    Point2D getVector(Direction direction);

    Point2D getOrigin();
}
