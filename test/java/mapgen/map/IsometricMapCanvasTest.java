package mapgen.map;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static mapgen.map.CardinalDirection.EAST;
import static mapgen.map.CardinalDirection.NORTH;
import static mapgen.map.CardinalDirection.WEST;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

// TODO javafx startup is too damn slow!
@RunWith(MockitoJUnitRunner.class)
class IsometricMapCanvasTest {

    private static final double GRID_SIZE = 50;

    Pane pane;
    IsometricMapCanvas map;

    @BeforeEach
    public void setUp() {
        this.pane = spy(new Pane());
        this.map = new IsometricMapCanvas(pane, GRID_SIZE);
    }

    @Test
    void addStairs() {
    }

    @Test
    void addDoor() {
    }

    @Test
    void addSingleRoom() {
        map.addRoom(null, new BaseRoom(5, 5));
        assertEquals("Should add room as single group.", 1, pane.getChildren().size());
        final Group roomShapes = (Group) pane.getChildren().get(0);
        assertEquals("room#0", roomShapes.getId());
        assertEquals(15, roomShapes.getChildren().size());
        final Polygon floorShape = (Polygon) roomShapes.getChildren().get(0);
        final double[] actualPoints = floorShape.getPoints().stream().mapToDouble(d -> d).toArray();
        assertArrayEquals(new double[]{0, 50, -125, 0, 0, -50, 125, 0}, actualPoints, 0);
    }

    @Test
    void addRooms_basic() {
        map.addRoom(null, new BaseRoom(5, 15));
        map.addRoom(new TestRoomPointer(EAST, null), new BaseRoom(7, 3));
        map.addRoom(new TestRoomPointer(NORTH, 2), new BaseRoom(1, 4));
        map.addRoom(new TestRoomPointer(NORTH, null), new BaseRoom(5, 5));
        map.addRoom(new TestRoomPointer(WEST, 2), new BaseRoom(5, 3));
        final double[] points = pane.getChildren().stream()
                .flatMapToDouble(room -> ((Polygon)((Group) room).getChildren().get(0)).getPoints().stream().mapToDouble(d -> d))
            .toArray();
        assertArrayEquals(new double[]{-125.0,100.0,-250.0,50.0,125.0,-100.0,250.0,-50.0,200.0,110.0,25.0,40.0,100.0,
                10.0,275.0,80.0,175.0,40.0,150.0,30.0,250.0,-10.0,275.0,0.0,325.0,20.0,200.0,-30.0,325.0,-80.0,450.0,
                -30.0,225.0,-40.0,100.0,-90.0,175.0,-120.0,300.0,-70.0}, points, 0);
    }

    @Test
    void clear() {
        pane.getChildren().add(new Line());
        map.clear();
        assertEquals(0, pane.getChildren().size());
        assertNull(map.getRoot());
    }

    static class TestRoomPointer implements RoomPointer {

        final Optional<CardinalDirection> direction;
        final RelativeLevel level;
        final Optional<Integer> parentIndex, childIndex;

        public TestRoomPointer(CardinalDirection direction, Integer parentIndex) {
            this(Optional.ofNullable(direction), RelativeLevel.SAME_LEVEL, Optional.ofNullable(parentIndex), Optional.empty());
        }

        public TestRoomPointer(Optional<CardinalDirection> direction,
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
        public Optional<CardinalDirection> getDirection() {
            return direction;
        }

        @Override
        public Optional<Integer> getIndex() {
            return parentIndex;
        }
    }

}