package darch.cmd;

import darch.map.*;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapCommands {

    private static final Pattern ROOM_LOC_PATTERN = Pattern.compile("(\\p{Alpha})?(?:(\\d+)(?:,(\\d+))?)?(\\+\\+|--)?");

    public static Format<MapCommand> getDefaultFormat(MapCanvas mapCanvas) {
        return new ExtensibleMapCommandFormat()
                .put("(\\d+)\\s*x\\s*(\\d+)((\\s*\\p{Alpha}((?:\\d+)?(?:,\\d+)?)?)?(?:\\+\\+|--)?)?", m -> {
                    final int depth = Integer.parseInt(m.group(1)), length = Integer.parseInt(m.group(2));
                    final Room parent = mapCanvas.getCurrentRoom();
                    final RelativeRoomLocation location = getRoomLocation(parent, m.group(3), depth, length);
                    final BaseRoom newRoom = new BaseRoom(parent, location, depth, length);
                    return new AddRoomCommand(newRoom);
                })
                .put("s(?:tairs)?\\s*(\\d+)(\\p{Alpha})", m -> new AddStairsCommand(getWallPointer(mapCanvas, m.group(1), m.group(2))))
                .put("d(?:oor)?\\s*(\\d+)(\\p{Alpha})", m -> new AddDoorCommand(getWallPointer(mapCanvas, m.group(1), m.group(2))))
                .put("(\\d+)?(\\p{Alpha})", m -> new GoCommand(getWallPointer(mapCanvas, m.group(1), m.group(2))))
                .put("back", m -> new BackCommand())
                .put("RESET|CLEAR|REFRESH", m -> new ClearCommand())
                .put("drop\\s*(\\p{Alpha})", m -> new DropWallCommand(CardinalPoint.byChar(m.group(1).charAt(0))));
    }

    private static WallLocation getWallPointer(MapCanvas mapCanvas, String indexStr, String directionStr) {
        final CardinalPoint direction = directionStr != null
                ? CardinalPoint.byChar(directionStr.charAt(0))
                : randomDirection();
        final int index = indexStr != null
                ? Integer.parseInt(indexStr) - 1
                : getMiddleIndex(direction, mapCanvas.getCurrentRoom());
        return new WallLocation(direction, index);
    }

    private static RelativeRoomLocation getRoomLocation(Room parent, String locationString, Integer x, Integer y) {
        if (locationString == null || parent == null)
            return null;
        final Matcher locMatcher = ROOM_LOC_PATTERN.matcher(locationString);
        if (!locMatcher.matches())
            throw new IllegalStateException("Location matcher didn't match?");
        final CardinalPoint direction = Optional.ofNullable(locMatcher.group(1))
                .map(l -> CardinalPoint.byChar(l.charAt(0)))
                .orElseGet(MapCommands::randomDirection);
        final int index = Optional.ofNullable(locMatcher.group(2))
                .map(str -> Integer.parseInt(str) - 1)
                .orElseGet(() -> getMiddleIndex(direction, parent));
        final int childIndex = Optional.ofNullable(locMatcher.group(3))
                .map(str -> Integer.parseInt(str) - 1)
                .orElseGet(() -> getMiddleIndex(direction, x, y));
        final RelativeLevel level = getRelativeLevel(Optional.ofNullable(locMatcher.group(4)).orElse(""));
        return new RelativeRoomLocation(direction, index, childIndex, level);
    }

    private static int getMiddleIndex(CardinalPoint direction, Room room) {
        return getMiddleIndex(direction, room.getDepth(), room.getLength());
    }
    private static int getMiddleIndex(CardinalPoint direction, Integer x, Integer y) {
        return (int) (Math.abs(direction.rotate90().getVector().dotProduct(x, y)) / 2);
    }

    private static RelativeLevel getRelativeLevel(String input) {
        switch (input) {
            case "++": return RelativeLevel.ABOVE;
            case "--": return RelativeLevel.BELOW;
            default: return RelativeLevel.SAME_LEVEL;
        }
    }

    private static CardinalPoint randomDirection() {
        return CardinalPoint.values()[(int) (Math.random() * 4d)];
    }

    static class AddRoomCommand implements MapCommand {

        final Room room;

        public AddRoomCommand(Room room) {
            this.room = room;
        }

        @Override
        public String asString() {
            return room.getDepth() + "x" + room.getLength() + locationString();
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

    static class AddStairsCommand implements MapCommand {

        final WallLocation wallLocation;

        public AddStairsCommand(WallLocation wallLocation) {
            this.wallLocation = wallLocation;
        }

        @Override
        public String asString() {
            return "s"+(wallLocation.getIndex()+1)+wallLocation.getDirection().name().subSequence(0,1);
        }

        @Override
        public void execute(MapCanvas map) {
            map.addStairs(wallLocation);
        }

        @Override
        public MapCommand reverse() {
            throw new UnsupportedOperationException(); // TODO
        }

    }

    static class AddDoorCommand implements MapCommand {

        final WallLocation wallLocation;

        public AddDoorCommand(WallLocation wallLocation) {
            this.wallLocation = wallLocation;
        }

        @Override
        public String asString() {
            return "d"+(wallLocation.getIndex()+1)+wallLocation.getDirection().name().subSequence(0,1);
        }

        @Override
        public void execute(MapCanvas map) {
            map.addDoor(wallLocation);
        }

        @Override
        public MapCommand reverse() {
            throw new UnsupportedOperationException(); // TODO
        }

    }

    static class ClearCommand implements MapCommand {

        @Override
        public String asString() {
            return "clear";
        }

        @Override
        public void execute(MapCanvas map) {
            map.clear();
        }

        @Override
        public MapCommand reverse() {
            throw new UnsupportedOperationException(); // TODO
        }

    }

    static class GoCommand implements MapCommand {

        final WallLocation wallLocation;

        public GoCommand(WallLocation wallLocation) {
            this.wallLocation = wallLocation;
        }

        @Override
        public String asString() {
            return (wallLocation.getIndex()+1)+wallLocation.getDirection().name().substring(0,1);
        }

        @Override
        public void execute(MapCanvas map) {
            map.go(wallLocation);
        }

        @Override
        public MapCommand reverse() {
            return new MapCommand() {
                @Override
                public String asString() {
                    return "back";
                }
                @Override
                public void execute(MapCanvas map) {
                    map.back();
                }
                @Override
                public MapCommand reverse() {
                    return GoCommand.this;
                }
            };
        }
    }

    static class BackCommand implements MapCommand {

        @Override
        public String asString() {
            return "back";
        }

        @Override
        public void execute(MapCanvas map) {
            map.back();
        }

        @Override
        public MapCommand reverse() {
            throw new UnsupportedOperationException();
        }

    }

    private static class DropWallCommand implements MapCommand {

        final CardinalPoint direction;

        public DropWallCommand(CardinalPoint direction) {
            this.direction = direction;
        }

        @Override
        public String asString() {
            return "drop" + direction.name().charAt(0);
        }

        @Override
        public void execute(MapCanvas map) {
            map.dropWall(direction);
        }

        @Override
        public MapCommand reverse() {
            throw new UnsupportedOperationException();
        }

    }
}
