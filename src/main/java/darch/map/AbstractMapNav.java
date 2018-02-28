package darch.map;

import darch.fx.Tracer;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

public abstract class AbstractMapNav implements MapNav {

    protected final double gridSize;

    public AbstractMapNav(double gridSize) {
        this.gridSize = gridSize;
    }

    @Override
    public Tracer<Point2D> points(Point2D start) {
        return new Tracer<Point2D>() {

            Point2D p = start;

            @Override
            public Point2D last() {
                return p;
            }

            @Override
            public Tracer<Point2D> then(double distance, Direction direction) {
                if (direction != null && distance != 0)
                    p = p.add(getVector(direction).multiply(distance));
                return this;
            }

            @Override
            public Point2D get() {
                return p;
            }

        };
    }

    @Override
    public Tracer<Polygon> poly(Point2D start, String... styleClasses) {
        final List<Point2D> points = new ArrayList<Point2D>();
        points.add(start);
        return new Tracer<Polygon>() {

            @Override
            public Tracer<Polygon> then(double distance, Direction direction) {
                points.add(last().add(getVector(direction).multiply(distance)));
                return this;
            }

            @Override
            public Point2D last() {
                return points.get(points.size() - 1);
            }

            @Override
            public Polygon get() {
                final Polygon polygon = new Polygon(points.stream()
                        .flatMapToDouble(p -> DoubleStream.of(p.getX(), p.getY()))
                        .toArray());
                polygon.getStyleClass().addAll(styleClasses);
                return polygon;
            }

        };
    }

    @Override
    public Point2D relativePoint(Point2D p, double distance, CardinalPoint direction) {
        return relativePoint(p, distance, direction.asDirection());
    }

    @Override
    public Point2D relativePoint(Point2D p, double distance, Direction direction) {
        return p.add(getVector(direction).multiply(distance));
    }

}
