package sample.map;

import javafx.scene.canvas.Canvas;

public interface MapCanvas {

    void drawRoom(RoomLocation location, Room room);

    Canvas getCanvas();

}
