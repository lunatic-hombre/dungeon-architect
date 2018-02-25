package darch.map;

import darch.collection.Lists;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import darch.collection.History;
import darch.collection.LinkedListHistory;
import darch.fx.Tracer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.DoubleStream;

import static darch.fx.ShapeUtils.line;
import static darch.fx.ShapeUtils.move;
import static darch.map.Direction.*;
import static darch.math.Matrices.multiply;
import static darch.math.Matrices.reflect;

public class IsoMap implements MapCanvas {

    private static final double XSCALE = 2, YSCALE = 5, ROOM_HEIGHT = 2;
    private static final int STAIR_COUNT = 8;
    private static final double STAIR_SIZE = ROOM_HEIGHT / (double) STAIR_COUNT;
    private static final String ROOM_KEY = "room";
    public static final Comparator<Node> ISO_ROOM_SORT = Comparator.<Node>comparingInt(n -> getRoom(n).getLevel())
            .thenComparing(n -> {
                Room room = getRoom(n);
                return room.getLatitude() + room.getLongitude();
            });

    private final Pane canvas;
    private final double gridSize;
    private final History<IsoRoom> rooms;
    private IsoRoom current;
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
        setCurrent(new IsoRoom(current, room));
        Lists.add(canvas.getChildren(), current.getUI(), ISO_ROOM_SORT);
        rooms.add(current);
    }

    private static Room getRoom(Node n) {
        return (Room) n.getProperties().get(ROOM_KEY);
    }

    private void setCurrent(IsoRoom room) {
        current = room;
        adjustViewport();
    }

    private void adjustViewport() {
        final Point2D midPoint = current.getMidPoint();
        final TranslateTransition translate = new TranslateTransition(Duration.millis(300), canvas);
        translate.setToX(canvas.getWidth()/2d - midPoint.getX());
        translate.setToY(canvas.getHeight()/2d - midPoint.getY());
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
        final Polygon door = poly(shift(start, 0.25, sideways), "door")
                .then(ROOM_HEIGHT-.5, UP)
                .then(.5, sideways)
                .then(ROOM_HEIGHT-.5, DOWN)
                .get();
        current.add(door);
    }

    private Point2D getPointOnWall(WallLocation location) {
        final Direction direction = location.getDirection().asDirection();
        final double halfDimension = Math.abs(current.getDimensions().dotProduct(direction.rotate90().getVector())) / 2;
        return shift(current.getMidWall(location.getDirection()), halfDimension - (double) location.getIndex(), direction.fallBack());
    }

    /*
     *   Content Removal
     *   ===============
     */
    @Override
    public void clear() {
        this.canvas.getChildren().clear();
    }

    @Override
    public void deleteRoom() {
        this.canvas.getChildren().remove(current.getUI());
        this.current = current.parent;
    }

    /*
     *   Privates
     *   ========
     */
    private Point2D getIsoVector(Direction direction) {
        return direction.isElevation()
                ? new Point2D(0, direction.getElevation()*ROOM_HEIGHT/YSCALE)
                : multiply(direction.rotate45().getVector(), 1/XSCALE, 1/YSCALE);
    }

    private Point2D shift(Point2D p, double distance, Direction direction) {
        return p.add(getIsoVector(direction).multiply(distance*gridSize));
    }

    /**
     * Navigate to a point by grid increments.
     */
    private Tracer<Point2D> points(Point2D start) {
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

    /**
     * Generate polygon by grid increments.
     */
    private Tracer<Polygon> poly(Point2D start, String... styleClasses) {
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

    class IsoRoom implements Room {

        final IsoRoom parent;
        final Room room;
        final List<ObjectReference> objectReferences;

        transient Point2D midPoint;
        transient Group ui;

        public IsoRoom(IsoRoom parent, Room room) {
            this.parent = parent;
            this.room = room;
            this.objectReferences = new ArrayList<>();
            if (parent == null)
                this.midPoint = new Point2D(canvas.getWidth()/2d, canvas.getHeight()/2d);
            else {
                final Direction direction = getLocation().getDirection().asDirection();

                final double distanceToward = Math.abs(parent.getDimensions().dotProduct(direction.getVector()) / 2d + getDimensions().dotProduct(direction.getVector()) / 2d);
                final double parentOffset = Math.abs(reflect(parent.getDimensions()).dotProduct(direction.getVector()) / 2d) - (double)(getLocation().getParentIndex());
                final double childOffset = Math.abs(reflect(getDimensions()).dotProduct(direction.getVector()) / 2d) - (double)(getLocation().getChildIndex());

                this.midPoint = points(parent.getMidPoint())
                        .then(distanceToward, direction)
                        .then(parentOffset - childOffset, direction.fallBack())
                        .then(ROOM_HEIGHT, getLocation().getFloor().asDirection())
                        .get();
            }
        }

        // TODO must maintain reference to model for persistence
        public void add(Node... nodes) {
            getUI().getChildren().add(new Group(nodes));
        }

        public void add(Collection<Node> nodes) {
            getUI().getChildren().add(new Group(nodes));
        }

        public Group getUI() {

            if (ui != null)
                return ui;

            final Point2D southWest = points(midPoint)
                    .then(getHalfLength(), SOUTH)
                    .then(getHalfDepth(), WEST)
                    .get();

            final Group floor = new Group();
            final Polygon floorShape = poly(southWest, "floor-outline")
                    .then(getLength(), NORTH)
                    .then(getDepth(), EAST)
                    .then(getLength(), SOUTH)
                    .get();
            floor.getChildren().add(floorShape);

            final Group gridLines = new Group();
            for (int i=0; i < getLength(); i++) {
                final Point2D start = shift(southWest, i, NORTH);
                gridLines.getChildren().add(line(start, shift(start, getDepth(), EAST)));
            }
            for (int i=0; i < getDepth(); i++) {
                final Point2D start = shift(southWest, i, EAST);
                gridLines.getChildren().add(line(start, shift(start, getLength(), NORTH)));
            }
            gridLines.getStyleClass().add("grid-lines");
            floor.getChildren().add(gridLines);

            final Polygon westWall = poly(southWest, "west", "wall")
                    .then(ROOM_HEIGHT, UP)
                    .then(getLength(), NORTH)
                    .then(ROOM_HEIGHT, DOWN)
                    .get();
            final Polygon eastWall = poly(shift(southWest, getDepth(), EAST), "east", "wall")
                    .then(ROOM_HEIGHT, UP)
                    .then(getLength(), NORTH)
                    .then(ROOM_HEIGHT, DOWN)
                    .get();
            final Polygon northWall = poly(shift(southWest, getLength(), NORTH), "north", "wall")
                    .then(ROOM_HEIGHT, UP)
                    .then(getDepth(), EAST)
                    .then(ROOM_HEIGHT, DOWN)
                    .get();
            final Polygon southWall = poly(southWest, "south", "wall")
                    .then(ROOM_HEIGHT, UP)
                    .then(getDepth(), EAST)
                    .then(ROOM_HEIGHT, DOWN)
                    .get();

            ui = new Group(floor, westWall, northWall, eastWall, southWall);
            ui.getProperties().put(ROOM_KEY, this);

            return ui;
        }

        public Point2D getMidPoint() {
            return midPoint;
        }

        public Point2D getMidWall(CardinalPoint direction) {
            final Direction d = direction.asDirection();
            final double distance = Math.abs(getDimensions().dotProduct(d.getVector())/2d);
            return shift(getMidPoint(), distance, d);
        }

        public Line getWall(CardinalPoint direction) {
            final Point2D midWall = getMidWall(direction);
            final CardinalPoint d1 = direction.rotate90(), d2 = d1.reverse();
            final double distance = Math.abs(direction.getVector().dotProduct(getDimensions()) / 2d);
            return line(shift(midWall, distance, d1.asDirection()), shift(midWall, distance, d2.asDirection()));
        }

        @Override
        public Room getParent() {
            return room.getParent();
        }

        @Override
        public RelativeRoomLocation getLocation() {
            return room.getLocation();
        }

        @Override
        public int getLongitude() {
            return room.getLongitude();
        }

        @Override
        public int getLatitude() {
            return room.getLatitude();
        }

        @Override
        public int getLevel() {
            return room.getLevel();
        }

        @Override
        public int getDepth() {
            return room.getDepth();
        }

        @Override
        public int getLength() {
            return room.getLength();
        }

        public boolean isAdjacent(IsoRoom other, CardinalPoint direction) {
            if (this.equals(other))
                return false;
            final Line l1 = getWall(direction), l2 = other.getWall(direction.reverse());
            return l1.contains(l2.getStartX(), l2.getStartY())
                    || l1.contains(l2.getEndX(), l2.getEndY())
                    || l2.contains(l1.getStartX(), l1.getStartY())
                    || l2.contains(l1.getEndX(), l1.getEndY());
        }

    }


}
