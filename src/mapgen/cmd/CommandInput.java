package mapgen.cmd;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import mapgen.err.ErrorReporter;
import mapgen.map.*;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommandInput extends TextField {

    private static final Pattern
            RESET_CMD_PATTERN = Pattern.compile("RESET|CLEAR|REFRESH", Pattern.CASE_INSENSITIVE),
            ROOM_CMD_PATTERN = Pattern.compile("(\\d+)\\s*x\\s*(\\d+)((\\s*\\p{Alpha}((?:\\d+)?(?:,\\d+)?)?)?(?:\\+\\+|--)?)?", Pattern.CASE_INSENSITIVE),
            ROOM_LOC_PATTERN = Pattern.compile("(\\p{Alpha})?(?:(\\d+)(?:,(\\d+))?)?(\\+\\+|--)?"),
            STAIRS_PATTERN = Pattern.compile("s(?:tairs)?\\s*(\\d+)(\\p{Alpha})", Pattern.CASE_INSENSITIVE);

    private final MapCanvas mapCanvas;
    private final ErrorReporter errorReporter;

    public CommandInput(MapCanvas mapCanvas, ErrorReporter errorReporter) {
        this.mapCanvas = mapCanvas;
        this.errorReporter = errorReporter;
        this.setOnKeyPressed(this::onKeyPress);
    }

    private void onKeyPress(KeyEvent keyEvent) {
        final KeyCode code = keyEvent.getCode();
        if (code.equals(KeyCode.ENTER)) {
            try {
                interpretCommand(this.getText().trim());
            } catch (Exception e) {
                errorReporter.reportError(e);
            }
            this.clear();
        }
    }

    private void interpretCommand(String text) {
        Matcher matcher;
        if ((matcher = ROOM_CMD_PATTERN.matcher(text)).matches())
            mapCanvas.addRoom(getRoomLocation(matcher.group(3)),
                    new BaseRoom(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
        else if((matcher = STAIRS_PATTERN.matcher(text)).matches())
            mapCanvas.addStairs(getWallPointer(matcher.group(1), matcher.group(2)));
        else if (RESET_CMD_PATTERN.matcher(text).matches())
            mapCanvas.clear();
        else
            throw new UnsupportedOperationException("Unknown command \""+text+"\".");
    }

    private WallPointer getWallPointer(String index, String directionStr) {
        return new WallPointer() {
            @Override
            public Optional<CardinalDirection> getDirection() {
                return directionStr != null
                        ? Optional.of(CardinalDirection.byChar(directionStr.charAt(0)))
                        : Optional.empty();
            }

            @Override
            public Optional<Integer> getIndex() {
                return directionStr != null
                        ? Optional.of(Integer.parseInt(index)-1)
                        : Optional.empty();
            }
        };
    }

    private RoomPointer getRoomLocation(String locationString) {
        if (locationString == null)
            return new EmptyRoomPointer();
        final Matcher locMatcher = ROOM_LOC_PATTERN.matcher(locationString);
        if (!locMatcher.matches())
            throw new IllegalStateException("Location matcher didn't match?");
        return new RoomPointer() {
            @Override
            public Optional<CardinalDirection> getDirection() {
                final String group = locMatcher.group(1);
                return group != null
                        ? Optional.of(CardinalDirection.byChar(group.charAt(0)))
                        : Optional.empty();
            }
            @Override
            public Optional<Integer> getIndex() {
                final String group = locMatcher.group(2);
                return group != null
                        ? Optional.of(Integer.parseInt(locMatcher.group(2))-1)
                        : Optional.empty();
            }
            @Override
            public Optional<Integer> getChildIndex() {
                final String group = locMatcher.group(3);
                return group != null
                        ? Optional.of(Integer.parseInt(locMatcher.group(3))-1)
                        : Optional.empty();
            }

            @Override
            public RelativeLevel getLevel() {
                final String group = Optional.ofNullable(locMatcher.group(4)).orElse("");
                switch (group) {
                    case "++": return RelativeLevel.ABOVE;
                    case "--": return RelativeLevel.BELOW;
                    default: return RelativeLevel.SAME_LEVEL;
                }
            }
        };
    }

    static class EmptyRoomPointer implements RoomPointer {
        @Override
        public Optional<CardinalDirection> getDirection() {
            return Optional.empty();
        }
        @Override
        public Optional<Integer> getIndex() {
            return Optional.empty();
        }
        @Override
        public Optional<Integer> getChildIndex() {
            return Optional.empty();
        }
        @Override
        public RelativeLevel getLevel() {
            return RelativeLevel.SAME_LEVEL;
        }
    }

}
