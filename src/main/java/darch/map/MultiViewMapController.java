package darch.map;

import darch.collection.History;
import darch.collection.LinkedListHistory;

import java.util.List;

import static java.util.Arrays.asList;

public class MultiViewMapController implements MapController {

    protected final List<MapView> views;
    protected final History<Room> rooms;

    protected Room current;

    public MultiViewMapController(MapView... views) {
        this(asList(views), new LinkedListHistory<>());
    }

    public MultiViewMapController(List<MapView> views, History<Room> rooms) {
        this.views = views;
        this.rooms = rooms;
    }

    @Override
    public Room getCurrentRoom() {
        return current;
    }

    @Override
    public void addRoom(Room room) {
        views.forEach(view -> view.drawRoom(room));
        rooms.add(setCurrent(room));
    }

    @Override
    public void addStairs(WallLocation location) {
        views.forEach(view -> view.drawStairs(current, location));
    }

    @Override
    public void addDoor(WallLocation location) {
        views.forEach(view -> view.drawDoor(current, location));
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

    @Override
    public void clear() {
        views.forEach(MapView::clear);
    }

    @Override
    public void deleteRoom() {
        views.forEach(view -> view.deleteRoom(current));
    }

    @Override
    public void dropWall(CardinalPoint direction) {
        views.forEach(view -> view.deleteWall(current, direction));
    }

    @Override
    public void centerViewPort(long transition) {
        // TODO maybe should just be in view interface
    }

    private Room setCurrent(Room room) {
        views.forEach(view -> view.setActive(room));
        return current = room;
    }

}
