package mapgen.map;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static com.sun.javafx.util.Utils.sum;
import static mapgen.fx.ShapeUtils.line;
import static mapgen.fx.ShapeUtils.polygon;
import static mapgen.map.CardinalDirection.*;
import static mapgen.math.Matrices.add;
import static mapgen.math.Matrices.multiply;
import static mapgen.math.Matrices.reflect;

public class IsometricMapCanvas implements MapCanvas {

    private static final double XSCALE = 2, YSCALE = 5, ROOM_HEIGHT = 2;
    private static final Color STROKE_COLOR = Color.BLACK;
    private static final Color GRID_LINE_COLOUR = Color.color(.2, .5, 1, 0.6);

    private final Pane canvas;
    private final double gridSize;
    private IsoRoom root, current;

    public IsometricMapCanvas(Pane canvas, double gridSize) {
        this.canvas = canvas;
        this.gridSize = gridSize;
    }

    @Override
    public void addStairs(WallPointer wallPointer) {
        if (current == null)
            throw new IllegalStateException();
        final CardinalDirection wall = wallPointer.getDirection().orElse(randomDirection());
        final int index = wallPointer.getIndex().orElse(wall.isVertical() ? current.getUnitLength()/2 : current.getUnitDepth()/2);
        final double offset = gridSize * index, stairLength = gridSize * ROOM_HEIGHT;
        final Shape stairs;
        if (wall.isVertical()) {
            final Point2D baseOfWall = current.getCorner(wall, WEST)
                    .add(getIsoVector(EAST).multiply(offset));
            final Point2D topOfStairwell = baseOfWall.add(0, ceilingHeight());
            final Point2D bottomOfStairwell = baseOfWall
                    .add(getIsoVector(wall).multiply(-stairLength));
            stairs = polygon(bottomOfStairwell, topOfStairwell,
                    topOfStairwell.add(getIsoVector(EAST).multiply(gridSize)),
                    bottomOfStairwell.add(getIsoVector(EAST).multiply(gridSize)));
        }
        else {
            final Point2D baseOfWall = current.getCorner(wall, SOUTH)
                    .add(getIsoVector(NORTH).multiply(offset));
            final Point2D topOfStairwell = baseOfWall.add(0, ceilingHeight());
            final Point2D bottomOfStairwell = baseOfWall
                    .add(getIsoVector(wall).multiply(-stairLength));
            stairs = polygon(bottomOfStairwell, topOfStairwell,
                    topOfStairwell.add(getIsoVector(NORTH).multiply(gridSize)),
                    bottomOfStairwell.add(getIsoVector(NORTH).multiply(gridSize)));
        }
        stairs.setStroke(Color.BLACK);
        stairs.setFill(Color.TRANSPARENT);
        draw(stairs);
    }

    @Override
    public void addRoom(RoomPointer location, Room room) {
        final IsoRoom newRoom = addNewRoom(room, getRelativeRoomLocation(location, current, room));
        drawRoom(newRoom);
    }

    private void drawRoom(IsoRoom room) {
        final Polygon floorShape = room.getPolygon();
        floorShape.setStroke(STROKE_COLOR);
        floorShape.setFill(Color.TRANSPARENT);
        draw(floorShape);
        draw(room.getGridLines());
        draw(
            room.getWall(WEST, Color.gray(0.5, 0.05)),
            room.getWall(NORTH, Color.gray(0.7, 0.05)),
            room.getWall(EAST, Color.TRANSPARENT),
            room.getWall(SOUTH, Color.TRANSPARENT)
        );
    }

    private IsoRoom addNewRoom(Room room, RelativeRoomLocation relativeLocation) {
        if (current == null)
            return current = root = new IsoRoom(current, relativeLocation, room);
        else
            return current = new IsoRoom(current, relativeLocation, room);
    }

    private void draw(Collection<? extends Node> nodes) {
        for (Node node : nodes)
            canvas.getChildren().add(node);
    }

    private void draw(Node... nodes) {
        for (Node node : nodes)
            canvas.getChildren().add(node);
    }

    @Override
    public void clear() {
        canvas.getChildren().clear();
        current = null;
    }

    private RelativeRoomLocation getRelativeRoomLocation(RoomPointer location, IsoRoom parent, Room room) {
        if (current == null)
            return null;
        final CardinalDirection direction = location.getDirection()
                .orElseGet(() -> randomDirection());
        final int parentIndex = location.getIndex()
                .orElseGet(() -> direction.isHorizontal() ? parent.getUnitLength()/2 : parent.getUnitDepth()/2);
        final int childIndex = location.getChildIndex()
                .orElseGet(() -> direction.isHorizontal() ? room.getLength()/2 : room.getDepth()/2);
        return new RelativeRoomLocation(direction, parentIndex, childIndex, location.getLevel());
    }

    private CardinalDirection randomDirection() {
        return CardinalDirection.values()[(int) (Math.random() * 4d)];
    }

    private double ceilingHeight() {
        return -(ROOM_HEIGHT*gridSize)/2;
    }

    private Point2D getIsoVector(CardinalDirection direction) {
        Point2D vector = direction.getVector();
        vector = direction.isVertical() ? vector.add(reflect(vector).multiply(-1)) : vector.add(reflect(vector));
        return multiply(vector, 1/XSCALE, 1/YSCALE);
    }

    class IsoRoom {

        final IsoRoom parentRoom;
        final RelativeRoomLocation location;
        final Room room;
        Point2D midpoint;

        public IsoRoom(IsoRoom parentRoom, RelativeRoomLocation location, Room room) {
            this.parentRoom = parentRoom;
            this.location = location;
            this.room = room;
        }

        double[] getDimensions() {
            return new double[] {getDepth(), getLength()};
        }

        double getLength() {
            return gridSize*room.getLength();
        }

        double getDepth() {
            return gridSize*room.getDepth();
        }

        int getUnitLength() {
            return room.getLength();
        }

        int getUnitDepth() {
            return room.getDepth();
        }

        Polygon getPolygon() {
            final double[] corners = getCorners()
                    .flatMapToDouble(point -> DoubleStream.of(point.getX(), point.getY()))
                    .toArray();
            return new Polygon(corners);
        }

        private Stream<Point2D> getCorners() {
            return Stream.of(getCorner(SOUTH, EAST), getCorner(SOUTH, WEST), getCorner(NORTH, WEST), getCorner(NORTH, EAST));
        }

        Point2D getCorner(CardinalDirection yDirection, CardinalDirection xDirection) {
            final double[] multiplier = add(xDirection.getMatrix(), yDirection.getMatrix());
            final double[] dimensions = getDimensions();
            final double[] difference = new double[] {
                    sum(multiply(dimensions, multiplier[0], -multiplier[1])),
                    sum(multiply(dimensions, multiplier))
            };
            final double[] scaled = multiply(difference, 1d/XSCALE, 1d/YSCALE);
            return getMidpoint().add(scaled[0], scaled[1]);
        }

        List<Line> getGridLines() {
            final double depth = getDepth(), length = getLength();
            final List<Line> lines = new ArrayList<>(room.getDepth()+room.getLength());
            for (double d = 0; d < depth; d+=gridSize) {
                lines.add(line(
                    getCorner(SOUTH, WEST).add(d/XSCALE, d/YSCALE),
                    getCorner(NORTH, WEST).add(d/XSCALE, d/YSCALE)
                ));
            }
            for (double d = 0; d < length; d+=gridSize) {
                lines.add(line(
                        getCorner(NORTH, WEST).add(-d/XSCALE, d/YSCALE),
                        getCorner(NORTH, EAST).add(-d/XSCALE, d/YSCALE)
                ));
            }
            lines.forEach(line -> line.setStroke(GRID_LINE_COLOUR));
            return lines;
        }

        Polygon getWall(CardinalDirection direction, Paint paint) {
            final Polygon wall;
            if (direction.isVertical()) {
                wall = polygon(
                        getCorner(direction, WEST),
                        getCorner(direction, WEST).add(0, ceilingHeight()),
                        getCorner(direction, EAST).add(0, ceilingHeight()),
                        getCorner(direction, EAST));
            } else {
                wall = polygon(
                        getCorner(NORTH, direction),
                        getCorner(NORTH, direction).add(0, ceilingHeight()),
                        getCorner(SOUTH, direction).add(0, ceilingHeight()),
                        getCorner(SOUTH, direction));
            }
            wall.setStroke(GRID_LINE_COLOUR.grayscale().brighter());
            wall.setFill(paint);
            return wall;
        }

        Point2D getMidpoint() {
            if (midpoint != null)
                return midpoint;
            else if (parentRoom == null)
                return midpoint = new Point2D(canvas.getWidth()/2d, canvas.getHeight()/2d);
            midpoint = getMidpoint(location.getDirection(), gridSize*location.getParentIndex(), gridSize*location.getChildIndex());
            if (!location.getFloor().equals(RelativeLevel.SAME_LEVEL))
                midpoint = midpoint.add(0, -(ROOM_HEIGHT*gridSize*location.getFloor().getScale())/2);
            return midpoint;
        }

        // TODO child offset
        Point2D getMidpoint(CardinalDirection wallDirection, double parentOffset, double childOffset) {

            final CardinalDirection vertical, horizontal;
            final double halfGrid = gridSize / 2, centeringDistance;
            final Point2D wallVector = wallDirection.getVector(),
                          offsetVector,
                          centeringDirectionVector;
            if (wallDirection.isVertical()) {
                vertical = wallDirection;
                horizontal = WEST;
                offsetVector = multiply(getVector(SOUTH, EAST), (parentOffset + halfGrid) / XSCALE, (parentOffset + halfGrid) / YSCALE);
                centeringDirectionVector = wallVector.add(wallVector.dotProduct(0, -1), 0);
                centeringDistance = getLength() / 2d;
            } else {
                vertical = SOUTH;
                horizontal = wallDirection;
                offsetVector = multiply(getVector(NORTH, EAST), (parentOffset + halfGrid) / XSCALE, (parentOffset + halfGrid) / YSCALE);
                centeringDirectionVector = wallVector.add(0, wallVector.dotProduct(1, 0));
                centeringDistance = getDepth() / 2d;
            }

            return parentRoom.getCorner(vertical, horizontal)
                    .add(offsetVector)
                    .add(multiply(centeringDirectionVector, centeringDistance / XSCALE, centeringDistance / YSCALE));
        }

    }

    static class RelativeRoomLocation {

        final CardinalDirection direction;
        final int parentIndex, childIndex;
        final RelativeLevel floor;

        public RelativeRoomLocation(CardinalDirection direction, int parentIndex, int childIndex, RelativeLevel floor) {
            this.direction = direction;
            this.parentIndex = parentIndex;
            this.childIndex = childIndex;
            this.floor = floor;
        }

        public CardinalDirection getDirection() {
            return direction;
        }

        public int getParentIndex() {
            return parentIndex;
        }

        public int getChildIndex() {
            return childIndex;
        }

        public RelativeLevel getFloor() {
            return floor;
        }
    }



}
