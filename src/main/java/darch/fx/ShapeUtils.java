package darch.fx;

import javafx.geometry.Point2D;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class ShapeUtils {

    public static final Line line(Point2D start, Point2D end, Paint paint) {
        final Line line = line(start, end);
        line.setStroke(paint);
        return line;
    }

    public static final Line line(Point2D start, Point2D end) {
        return new Line(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public static double[] flatten(Point2D... points) {
        return Stream.of(points)
                .flatMapToDouble(p -> DoubleStream.of(p.getX(), p.getY()))
                .toArray();
    }

    public static Polygon polygon(Point2D... points) {
        return new Polygon(flatten(points));
    }

    public static void move(Shape shape, Point2D vector) {
        shape.setTranslateX(vector.getX());
        shape.setTranslateY(vector.getY());
    }

}
