package darch.map;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class GenericMapCanvasTest {


    private static final double GRID_SIZE = 50;

    Pane pane;
    GenericMapCanvas map;

    @Before
    public void setUp() {
        this.pane = spy(new Pane());
        this.map = new GenericMapCanvas(pane, new IsoMapNav(GRID_SIZE));
    }

    @Test
    public void addStairs() {
    }

    @Test
    public void addDoor() {
    }

    @Test
    public void addSingleRoom() {
        map.addRoom(new BasicRoom(5, 5));
        assertEquals("Should add room as single group.", 1, pane.getChildren().size());
        final Group roomShapes = (Group) pane.getChildren().get(0);
        assertEquals(5, roomShapes.getChildren().size());
        final Polygon floorShape = ((Polygon) ((Group) roomShapes.getChildren().get(0)).getChildren().get(0));
        final double[] actualPoints = floorShape.getPoints().stream().mapToDouble(d -> d).toArray();
        assertArrayEquals(new double[]{-125, 0, 0, -50, 125, 0, 0, 50}, actualPoints, 0);
    }

    @Test
    public void addRooms_basic() {
        map.addRoom(new BasicRoom(5, 15));
//        map.addRoom(new RelativeRoomLocation(EAST, -1, -1, RelativeLevel.SAME_LEVEL), new BaseRoom(7, 3));
//        map.addRoom(new RelativeRoomLocation(NORTH, 2, -1, RelativeLevel.SAME_LEVEL), new BaseRoom(1, 4));
//        map.addRoom(new RelativeRoomLocation(NORTH, -1, -1, RelativeLevel.SAME_LEVEL), new BaseRoom(5, 5));
//        map.addRoom(new RelativeRoomLocation(WEST, 2, -1, RelativeLevel.SAME_LEVEL), new BaseRoom(5, 3));
        // TODO
        final double[] points = pane.getChildren().stream()
                .flatMapToDouble(room -> ((Polygon)((Group)((Group) room).getChildren().get(0)).getChildren().get(0)).getPoints().stream().mapToDouble(d -> d))
                .toArray();
        // TODO assert
    }

    @Test
    public void clear() {
        pane.getChildren().add(new Line());
        map.clear();
        assertEquals(0, pane.getChildren().size());
    }

    static class TestRoomPointer implements RoomPointer {

        final Optional<CardinalPoint> direction;
        final RelativeLevel level;
        final Optional<Integer> parentIndex, childIndex;

        public TestRoomPointer(CardinalPoint direction, Integer parentIndex) {
            this(Optional.ofNullable(direction), RelativeLevel.SAME_LEVEL, Optional.ofNullable(parentIndex), Optional.empty());
        }

        public TestRoomPointer(Optional<CardinalPoint> direction,
                               RelativeLevel level,
                               Optional<Integer> parentIndex,
                               Optional<Integer> childIndex) {
            this.direction = direction;
            this.level = level;
            this.parentIndex = parentIndex;
            this.childIndex = childIndex;
        }

        @Override
        public Optional<Integer> getChildIndex() {
            return childIndex;
        }

        @Override
        public RelativeLevel getLevel() {
            return level;
        }

        @Override
        public Optional<CardinalPoint> getDirection() {
            return direction;
        }

        @Override
        public Optional<Integer> getIndex() {
            return parentIndex;
        }
    }

}
