package mapgen.map;

public interface MapCanvas {

    void addRoom(RoomPointer location, Room room);

    void addStairs(WallPointer wallPointer);

    void clear();

}
