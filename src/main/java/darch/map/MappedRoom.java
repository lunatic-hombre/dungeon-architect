package darch.map;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static darch.fx.ShapeUtils.line;
import static darch.map.Direction.*;
import static darch.map.MapConstants.ROOM_HEIGHT;
import static darch.map.MapConstants.ROOM_KEY;
import static darch.math.Matrices.reflect;
import static java.util.Arrays.asList;

public class MappedRoom implements Room {

    final MapNav map;
    final Room room;
    final List<ObjectReference> objectReferences;

    transient Point2D midPoint;
    transient Group ui;

    public MappedRoom(MapNav map, Point2D position, Room room) {
        this.map = map;
        this.room = room;
        this.objectReferences = new ArrayList<>();
        final Room parent = room.getParent();
        if (room.getParent() == null)
            this.midPoint = position;
        else {
            final Direction direction = getLocation().getDirection().asDirection();

            final double distanceToward = Math.abs(parent.getDimensions().dotProduct(direction.getVector()) / 2d + getDimensions().dotProduct(direction.getVector()) / 2d);
            final double parentOffset = Math.abs(reflect(parent.getDimensions()).dotProduct(direction.getVector()) / 2d) - (double)(getLocation().getParentIndex());
            final double childOffset = Math.abs(reflect(getDimensions()).dotProduct(direction.getVector()) / 2d) - (double)(getLocation().getChildIndex());

            this.midPoint = map.points(position)
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

        final Point2D southWest = map.points(midPoint)
                .then(getHalfMeridian(), SOUTH)
                .then(getHalfHorizontal(), WEST)
                .get();

        final Group floor = new Group();
        final Polygon floorShape = map.poly(southWest, "floor-outline")
                .then(getMeridianScale(), NORTH)
                .then(getHorizontalScale(), EAST)
                .then(getMeridianScale(), SOUTH)
                .get();
        floor.getChildren().add(floorShape);

        final Group gridLines = new Group();
        for (int i = 0; i < getMeridianScale(); i++) {
            final Point2D start = map.relativePoint(southWest, i, NORTH);
            gridLines.getChildren().add(line(start, map.relativePoint(start, getHorizontalScale(), EAST)));
        }
        for (int i = 0; i < getHorizontalScale(); i++) {
            final Point2D start = map.relativePoint(southWest, i, EAST);
            gridLines.getChildren().add(line(start, map.relativePoint(start, getMeridianScale(), NORTH)));
        }
        gridLines.getStyleClass().add("grid-lines");
        floor.getChildren().add(gridLines);

        final List<Node> components = new ArrayList<>(4);
        components.add(floor);
        for (CardinalPoint cp : CardinalPoint.values())
            components.add(createWall(cp));

        ui = new Group(components);
        ui.getProperties().put(ROOM_KEY, this);

        return ui;
    }

    public Optional<Shape> getWall(CardinalPoint direction) {
        return getUI().getChildren().stream().filter(n -> n.getStyleClass()
                .containsAll(asList("wall", direction.name().toLowerCase())))
                .map(Shape.class::cast)
                .findAny();
    }

    private Polygon createWall(CardinalPoint direction) {
        final CardinalPoint perpendicular = direction.fallback();
        final Point2D startingCorner = map.relativePoint(getMidWall(direction),
                getDimension(perpendicular)/2d, perpendicular);
        // if adding wall from origin, avoid section already created by parent.
        // TODO just use substract
        if (direction.equals(getOrigin())) {
            if (getDimension(perpendicular) <= getParent().getDimension(perpendicular))
                return new Polygon(); // no wall
            return map.poly(startingCorner, direction.name().toLowerCase(), "wall", "origin")
                    .then(ROOM_HEIGHT, UP)
                    .then(getLocation().getChildIndex() - getLocation().getParentIndex(), perpendicular.reverse())
                    .then(ROOM_HEIGHT, DOWN)
                    .then(getParent().getDimension(perpendicular), perpendicular.reverse())
                    .then(ROOM_HEIGHT, UP)
                    .then(getDimension(perpendicular) - getParent().getDimension(perpendicular) - (getLocation().getChildIndex() - getLocation().getParentIndex()), perpendicular.reverse())
                    .then(ROOM_HEIGHT, DOWN)
                    .get();
        } else {
            return buildWall(direction);
        }
    }

    Polygon buildWall(CardinalPoint direction) {
        final CardinalPoint perpendicular = direction.fallback();
        final Point2D startingCorner = map.relativePoint(getMidWall(direction),
                getDimension(perpendicular)/2d, perpendicular);
        return map.poly(startingCorner, direction.name().toLowerCase(), "wall")
                .then(ROOM_HEIGHT, UP)
                .then(getDimension(perpendicular), perpendicular.reverse())
                .then(ROOM_HEIGHT, DOWN)
                .get();
    }

    private CardinalPoint getOrigin() {
        return getParent() == null ? null : getLocation().getDirection().reverse();
    }

    public Point2D getMidPoint() {
        return midPoint;
    }

    public Point2D getMidWall(CardinalPoint direction) {
        final Direction d = direction.asDirection();
        final double distance = Math.abs(getDimensions().dotProduct(d.getVector())/2d);
        return map.relativePoint(getMidPoint(), distance, d);
    }

    public Line getBoundary(CardinalPoint direction) {
        final Point2D midWall = getMidWall(direction);
        final CardinalPoint d1 = direction.rotate90(), d2 = d1.reverse();
        final double distance = Math.abs(direction.getVector().dotProduct(getDimensions()) / 2d);
        return line(map.relativePoint(midWall, distance, d1.asDirection()), map.relativePoint(midWall, distance, d2.asDirection()));
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
    public int getHorizontalScale() {
        return room.getHorizontalScale();
    }

    @Override
    public int getMeridianScale() {
        return room.getMeridianScale();
    }

    public boolean isAdjacent(MappedRoom other, CardinalPoint direction) {
        if (this.equals(other))
            return false;
        final Line l1 = getBoundary(direction), l2 = other.getBoundary(direction.reverse());
        return l1.contains(l2.getStartX(), l2.getStartY())
                || l1.contains(l2.getEndX(), l2.getEndY())
                || l2.contains(l1.getStartX(), l1.getStartY())
                || l2.contains(l1.getEndX(), l1.getEndY());
    }

}
