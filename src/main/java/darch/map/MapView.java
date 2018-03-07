package darch.map;

import javafx.scene.layout.Pane;

public interface MapView {

    void setActive(Room room);

    void drawRoom(Room room);

    void drawStairs(Room room, WallLocation location);

    void drawDoor(Room room, WallLocation location);

    void deleteRoom(Room room);

    void deleteWall(Room room, CardinalPoint direction);

    default void clear() {
        getUI().getChildren().clear();
    }

    Pane getUI();

}
