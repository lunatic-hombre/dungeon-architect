package darch.cmd.types;

import darch.cmd.MapCommand;
import darch.map.MapCanvas;
import darch.map.RelativeLevel;
import darch.map.RelativeRoomLocation;
import darch.map.Room;

public class AddRoomCommand implements MapCommand {

    final Room room;

    public AddRoomCommand(Room room) {
        this.room = room;
    }

    @Override
    public String asString() {
        return room.getHorizontalScale() + "x" + room.getMeridianScale() + locationString();
    }

    private String locationString() {
        final RelativeRoomLocation location = room.getLocation();
        if (location == null)
            return "";
        String locationString = location.getDirection().name().substring(0, 1)
                + location.getParentIndex() + "," + location.getChildIndex();
        if (!location.getFloor().equals(RelativeLevel.SAME_LEVEL))
            locationString += location.getFloor().equals(RelativeLevel.ABOVE) ? "++" : "--";
        return locationString;
    }

    @Override
    public void execute(MapCanvas map) {
        map.addRoom(room);
    }

    @Override
    public MapCommand reverse() {
        return new MapCommand() {
            @Override
            public String asString() {
                return "^"+AddRoomCommand.this.asString();
            }
            @Override
            public void execute(MapCanvas map) {
                map.deleteRoom();
            }
            @Override
            public MapCommand reverse() {
                return AddRoomCommand.this;
            }
        };
    }
}
