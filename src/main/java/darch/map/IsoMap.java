package darch.map;

import darch.collection.Lists;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import darch.collection.History;
import darch.collection.LinkedListHistory;
import darch.fx.Tracer;

import java.util.*;
import java.util.stream.DoubleStream;

import static darch.fx.ShapeUtils.line;
import static darch.fx.ShapeUtils.move;
import static darch.map.Direction.*;
import static darch.map.MapConstants.ROOM_HEIGHT;
import static darch.map.MapConstants.ROOM_KEY;
import static darch.math.Matrices.multiply;

public class IsoMap implements MapCanvas, MapNav {

    private static final double XSCALE = 2, YSCALE = 5;
    private static final int STAIR_COUNT = 8;
    private static final double STAIR_SIZE = ROOM_HEIGHT / (double) STAIR_COUNT;
    public static final Comparator<Node> ISO_ROOM_SORT = Comparator.<Node>comparingInt(n -> getRoom(n).getLevel())
            .thenComparing(n -> {
                Room room = getRoom(n);
                return room.getLatitude() + room.getLongitude();
            });

    private final Pane canvas;
    private final double gridSize;
    private final History<MappedRoom> rooms;
    private Point2D position;
    private MappedRoom current;
    private int roomIncrement = 0; // TODO ids for rooms n stuff?

    public IsoMap(Pane canvas, double gridSize) {
        this.rooms = new LinkedListHistory<>();
        this.canvas = canvas;
        this.gridSize = gridSize;
    }

    /*
     *   Navigation
     *   ==========
     */

    @Override
    public Room getCurrentRoom() {
        return current == null ? null : current.room;
    }

    @Override
    public void back() {
        setCurrent(rooms.back());
    }

    @Override
    public void go(WallLocation location) {
        rooms.stream()
            .filter(room -> current.isAdjacent(room, location.getDirection()))
            .findAny()
            .ifPresent(this::setCurrent);
    }

    /*
     *   Content Addition
     *   ==================
     */
    @Override
    public void addRoom(Room room) {
        setCurrent(new MappedRoom(this, getPosition(), room));
        Lists.add(canvas.getChildren(), current.getUI(), ISO_ROOM_SORT);
        rooms.add(current);
    }

    public Point2D getPosition() {
        return position == null ? position = getMidPoint() : position;
    }

    private static Room getRoom(Node n) {
        return (Room) n.getProperties().get(ROOM_KEY);
    }

    private void setCurrent(MappedRoom room) {
        if (current != null)
            current.getUI().getStyleClass().remove("active");
        room.getUI().getStyleClass().add("active");
        current = room;
        position = room.getMidPoint();
        adjustViewport();
    }

    private void adjustViewport() {
        final TranslateTransition translate = new TranslateTransition(Duration.millis(300), canvas);
        translate.setToX(canvas.getWidth()/2d - position.getX());
        translate.setToY(canvas.getHeight()/2d - position.getY());
        translate.play();
    }

    @Override
    public void addStairs(WallLocation location) {
        final Direction d = location.getDirection().asDirection();
        final Direction sideways = location.getDirection().asDirection().fallBack().reverse();
        final Point2D start = getPointOnWall(location);
        final List<Node> stairShapes = new ArrayList<>();
        final Tracer<Polygon> side = poly(start, "stairs-side").then(ROOM_HEIGHT, d.reverse());
        for (int i = 0; i < STAIR_COUNT; i++) {
            stairShapes.add(poly(side.last(), "stair-up").then(STAIR_SIZE, UP).then(1, sideways).then(STAIR_SIZE, DOWN).get());
            side.then(STAIR_SIZE, UP).then(STAIR_SIZE, d);
            stairShapes.add(poly(side.last(), "stair-across").then(1, sideways).then(STAIR_SIZE, d.reverse()).then(1, sideways.reverse()).get());
        }
        final Polygon sideShape = side.then(ROOM_HEIGHT, DOWN).get();
        if (d.isY())
            move(sideShape, getIsoVector(sideways).multiply(gridSize));
        if (d.equals(SOUTH) || d.equals(EAST))
            stairShapes.add(poly(side.last(), "stairs-back").then(1, sideways).then(ROOM_HEIGHT, UP).then(1, sideways.reverse()).get());
        stairShapes.add(sideShape);
        current.add(stairShapes);
    }

    @Override
    public void addDoor(WallLocation location) {
        final Point2D start = getPointOnWall(location);
        final Direction sideways = location.getDirection().asDirection().fallBack().reverse();
        final Polygon door = poly(relativePoint(start, 0.25, sideways), "door")
                .then(ROOM_HEIGHT-.5, UP)
                .then(.5, sideways)
                .then(ROOM_HEIGHT-.5, DOWN)
                .get();
        current.add(door);
    }

    private Point2D getPointOnWall(WallLocation location) {
        final Direction direction = location.getDirection().asDirection();
        final double halfDimension = Math.abs(current.getDimensions().dotProduct(direction.rotate90().getVector())) / 2;
        return relativePoint(current.getMidWall(location.getDirection()), halfDimension - (double) location.getIndex(), direction.fallBack());
    }

    /*
     *   Content Removal
     *   ===============
     */
    @Override
    public void clear() {
        this.canvas.getChildren().clear();
        this.current = null;
    }

    @Override
    public void deleteRoom() {
        this.canvas.getChildren().remove(current.getUI());
        this.current = rooms.back();
    }

    @Override
    public void dropWall(CardinalPoint direction) {
        final Optional<Shape> wall = current.getWall(direction);
        if (wall.isPresent())
            current.getUI().getChildren().remove(wall.get());
        else {
            // drop contact section of parent's adjacent wall
            final Polygon subtractionWall = current.buildWall(direction);
            subtractionWall.setScaleY(1.1);
            rooms.stream().filter(room -> current.isAdjacent(room, direction)).forEach(room -> {
                room.getWall(direction.reverse()).ifPresent(poly -> {
                    room.getUI().getChildren().remove(poly);
                    final Shape updatedWall = Path.subtract(poly, subtractionWall);
                    updatedWall.getStyleClass().addAll(poly.getStyleClass());
                    room.getUI().getChildren().add(updatedWall);
                });
            });
        }
    }

    /*
     *   Navigation Support for Rooms
     *   ============================
     */
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
                    p = p.add(getIsoVector(direction).multiply(distance).multiply(gridSize));
                return this;
            }

            @Override
            public Point2D get() {
                return p;
            }

        };
    }

    @Override
    public Point2D getMidPoint() {
        return new Point2D(canvas.getWidth()/2d, canvas.getHeight()/2d);
    }

    @Override
    public Tracer<Polygon> poly(Point2D start, String... styleClasses) {
        final List<Point2D> points = new ArrayList<>();
        points.add(start);
        return new Tracer<Polygon>() {

            @Override
            public Tracer<Polygon> then(double distance, Direction direction) {
                points.add(last().add(getIsoVector(direction).multiply(distance).multiply(gridSize)));
                return this;
            }

            @Override
            public Point2D last() {
                return points.get(points.size()-1);
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
        return p.add(getIsoVector(direction).multiply(distance*gridSize));
    }

    private Point2D getIsoVector(Direction direction) {
        return direction.isElevation()
                ? new Point2D(0, direction.getElevation()*ROOM_HEIGHT/YSCALE)
                : multiply(direction.rotate45().getVector(), 1/XSCALE, 1/YSCALE);
    }


}
