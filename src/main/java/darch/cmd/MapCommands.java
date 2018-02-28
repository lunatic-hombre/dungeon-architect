package darch.cmd;

import darch.cmd.types.*;
import darch.map.*;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapCommands {

    private static final Pattern ROOM_LOC_PATTERN = Pattern.compile("(\\p{Alpha})?(?:(\\d+)(?:,(\\d+))?)?(\\+\\+|--)?");

    public static Format<MapCommand> getDefaultFormat(MapCanvas mapCanvas) {
        return new ExtensibleMapCommandFormat()
                .put("(\\d+)\\s*x\\s*(\\d+)((\\s*\\p{Alpha}((?:\\d+)?(?:,\\d+)?)?)?(?:\\+\\+|--)?)?", m -> {
                    final int horizontal = Integer.parseInt(m.group(1)), meridian = Integer.parseInt(m.group(2));
                    final Room parent = mapCanvas.getCurrentRoom();
                    final RelativeRoomLocation location = getRoomLocation(parent, m.group(3), horizontal, meridian);
                    final BasicRoom newRoom = new BasicRoom(parent, location, horizontal, meridian);
                    return new AddRoomCommand(newRoom);
                })
                .put("s(?:tairs)?\\s*(\\d+)(\\p{Alpha})", m -> new AddStairsCommand(getWallPointer(mapCanvas, m.group(1), m.group(2))))
                .put("d(?:oor)?\\s*(\\d+)(\\p{Alpha})", m -> new AddDoorCommand(getWallPointer(mapCanvas, m.group(1), m.group(2))))
                .put("(\\d+)?(\\p{Alpha})", m -> new GoTowardCommand(getWallPointer(mapCanvas, m.group(1), m.group(2))))
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
        return getMiddleIndex(direction, room.getHorizontal(), room.getMeridian());
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

}
