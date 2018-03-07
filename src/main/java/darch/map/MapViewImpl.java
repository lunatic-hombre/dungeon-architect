package darch.map;

import darch.collection.Lists;
import darch.fx.Tracer;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static darch.fx.ShapeUtils.line;
import static darch.fx.ShapeUtils.move;
import static darch.map.Direction.*;
import static darch.map.Direction.EAST;
import static darch.map.Direction.NORTH;
import static darch.map.MapConstants.ROOM_HEIGHT;
import static darch.map.MapConstants.ROOM_KEY;
import static java.util.Arrays.asList;

public class MapViewImpl implements MapView {

    protected static final int STAIR_COUNT = 8;
    protected static final double STAIR_SIZE = ROOM_HEIGHT / (double) STAIR_COUNT;
    protected static final Comparator<Node> ISO_ROOM_SORT = Comparator.<Node>comparingInt(n -> getRoom(n).getLevel())
            .thenComparing(n -> {
                Room room = getRoom(n);
                return room.getLatitude() + room.getLongitude();
            });

    private static Room getRoom(Node n) {
        return (Room) n.getProperties().get(ROOM_KEY);
    }

    protected final Pane canvas;
    protected final MapNav nav;

    public MapViewImpl(Pane canvas, MapNav nav) {
        this.canvas = canvas;
        this.nav = nav;
    }

    @Override
    public void setActive(Room room) {
        canvas.getChildren().forEach(n -> n.getStyleClass().remove("active"));
        final Group activeNode = getNode(room);
        activeNode.getStyleClass().add("active");
        centerCanvas(activeNode);
    }

    protected void centerCanvas(Node node) {
        final Bounds nodeBounds = node.getBoundsInParent();
        final TranslateTransition translate = new TranslateTransition(Duration.millis(150), canvas);
        Point2D offset = new Point2D(
                (canvas.getWidth() - nodeBounds.getMinX() - nodeBounds.getMaxX())/2d,
                (canvas.getHeight() - nodeBounds.getMinY() - nodeBounds.getMaxY())/2d);
        translate.setToX(offset.getX());
        translate.setToY(offset.getY());
        translate.play();
    }

    @Override
    public void drawRoom(Room room) {

        final Point2D midPoint = room.getParent() == null
                ? nav.getOrigin()
                : nav.translateGridPoint(room.getMidpoint(), room.getLevel() * ROOM_HEIGHT);

        final Point2D southWest = nav.points(midPoint)
                .then(room.getHalfMeridian(), SOUTH)
                .then(room.getHalfHorizontal(), WEST)
                .get();

        final Group floor = new Group();
        final Polygon floorShape = nav.poly(southWest, "floor-outline")
                .then(room.getMeridian(), NORTH)
                .then(room.getHorizontal(), EAST)
                .then(room.getMeridian(), SOUTH)
                .get();
        floor.getChildren().add(floorShape);

        final Group gridLines = new Group();
        for (int i = 0; i < room.getMeridian(); i++) {
            final Point2D start = nav.relativePoint(southWest, i, NORTH);
            gridLines.getChildren().add(line(start, nav.relativePoint(start, room.getHorizontal(), EAST)));
        }
        for (int i = 0; i < room.getHorizontal(); i++) {
            final Point2D start = nav.relativePoint(southWest, i, EAST);
            gridLines.getChildren().add(line(start, nav.relativePoint(start, room.getMeridian(), NORTH)));
        }
        gridLines.getStyleClass().add("grid-lines");
        floor.getChildren().add(gridLines);

        final List<Node> components = new ArrayList<>(4);
        components.add(floor);
        for (CardinalPoint cp : CardinalPoint.values())
            components.add(createWall(room, cp));

        final Group ui = new Group(components);
        ui.getProperties().put(ROOM_KEY, room);

        Lists.add(canvas.getChildren(), ui, ISO_ROOM_SORT);

    }

    private Polygon createWall(Room room, CardinalPoint direction) {
        final CardinalPoint perpendicular = direction.fallback();
        final Point2D startingCorner = nav.relativePoint(nav.translateGridPoint(room.getMidWall(direction), room.getLevel() * ROOM_HEIGHT),
                room.getDimension(perpendicular)/2d, perpendicular);
        return nav.poly(startingCorner, direction.name().toLowerCase(), "wall")
                .then(ROOM_HEIGHT, UP)
                .then(room.getDimension(perpendicular), perpendicular.reverse())
                .then(ROOM_HEIGHT, DOWN)
                .get();
    }

    @Override
    public void drawStairs(Room room, WallLocation location) {
        final Direction d = location.getDirection().asDirection();
        final Direction sideways = location.getDirection().asDirection().fallBack().reverse();
        final Point2D start = nav.translateGridPoint(room.getOffset(location), room.getLevel() * ROOM_HEIGHT);
        final List<Node> stairShapes = new ArrayList<>();
        final Tracer<Polygon> side = nav.poly(start, "stairs-side").then(ROOM_HEIGHT, d.reverse());
        for (int i = 0; i < STAIR_COUNT; i++) {
            stairShapes.add(nav.poly(side.last(), "stair-up").then(STAIR_SIZE, UP).then(1, sideways).then(STAIR_SIZE, DOWN).get());
            side.then(STAIR_SIZE, UP).then(STAIR_SIZE, d);
            stairShapes.add(nav.poly(side.last(), "stair-across").then(1, sideways).then(STAIR_SIZE, d.reverse()).then(1, sideways.reverse()).get());
        }
        final Polygon sideShape = side.then(ROOM_HEIGHT, DOWN).get();
        if (d.isY())
            move(sideShape, nav.getVector(sideways));
        if (d.equals(SOUTH) || d.equals(EAST))
            stairShapes.add(nav.poly(side.last(), "stairs-back").then(1, sideways).then(ROOM_HEIGHT, UP).then(1, sideways.reverse()).get());
        stairShapes.add(sideShape);
        getNode(room).getChildren().add(new Group(stairShapes));
    }

    @Override
    public void drawDoor(Room room, WallLocation location) {
        final Point2D start = nav.translateGridPoint(room.getOffset(location), room.getLevel() * ROOM_HEIGHT);
        final Direction sideways = location.getDirection().asDirection().fallBack().reverse();
        final Polygon door = nav.poly(nav.relativePoint(start, 0.25, sideways), "door")
                .then(ROOM_HEIGHT-.5, UP)
                .then(.5, sideways)
                .then(ROOM_HEIGHT-.5, DOWN)
                .get();
        getNode(room).getChildren().addAll(door);
    }

    @Override
    public void deleteRoom(Room room) {
        canvas.getChildren().removeIf(n -> isRoom(n, room));
    }

    @Override
    public void deleteWall(Room room, CardinalPoint direction) {

        // delete wall of room
        final Group roomNode = getNode(room);
        roomNode.getChildren().removeIf(child -> child.getStyleClass()
                .containsAll(asList("wall", direction.name().toLowerCase())));
        final Polygon subtractionWall = createWall(room, direction);
        subtractionWall.setScaleY(1.1);

        // remove section of wall from any adjacent rooms
        canvas.getChildren().stream().map(Group.class::cast).filter(n -> room.isAdjacent(getRoom(n), direction)).forEach(n -> {
            final Optional<Shape> oppositeWall = n.getChildren().stream().filter(child -> child.getStyleClass()
                    .containsAll(asList("wall", direction.reverse().name().toLowerCase())))
                    .map(Shape.class::cast)
                    .findAny();
            oppositeWall.ifPresent(w -> {
                n.getChildren().remove(w);
                final Shape updatedWall = Path.subtract(w, subtractionWall);
                updatedWall.getStyleClass().addAll(w.getStyleClass());
                n.getChildren().add(updatedWall);
            });
        });

    }

    private Group getNode(Room room) {
        return (Group) canvas.getChildren().stream()
                .filter(n -> isRoom(n, room))
                .findAny().orElse(null);
    }

    private boolean isRoom(Node n, Room room) {
        return n.getProperties().get(ROOM_KEY).equals(room);
    }

    @Override
    public Pane getUI() {
        return canvas;
    }

}
