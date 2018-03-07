package darch.map;

public interface MapController {

    /**
     * Get the current room details.
     */
    Room getCurrentRoom();

    /**
     * Add an adjacent room in the given location.
     */
    void addRoom(Room room);

    /**
     * Add stairs to current room at given wall location. Stairs will climb at 45 degree angle, so clearance is
     * required equal to the height of the room in the opposite direction of the wall.
     */
    void addStairs(WallLocation location);

    /**
     * Add door to current room at given wall location.
     */
    void addDoor(WallLocation location);

    /**
     * Go back to previous room.
     */
    void back();

    /**
     * Go to adjacent room in given direction, offset by index (room count from south/west).
     */
    void go(WallLocation location);

    /**
     * Remove everything from the nav.
     */
    void clear();

    /**
     * Delete the current room.
     */
    void deleteRoom();

    /**
     * Drop wall in given direction from current room.
     */
    void dropWall(CardinalPoint direction);

    /**
     * Move the viewport to the current logical position.
     * @param transition the milliseconds for the transition
     */
    void centerViewPort(long transition);

    /**
     * Move the viewport at a 1 millisecond transition time (effectively no animation).
     */
    default void centerViewPort() {
        centerViewPort(1);
    }
}
