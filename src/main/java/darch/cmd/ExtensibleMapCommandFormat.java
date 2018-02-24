package darch.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtensibleMapCommandFormat implements Format<MapCommand> {

    private final Map<Pattern, Function<Matcher, MapCommand>> commands;

    public ExtensibleMapCommandFormat() {
        this.commands = new HashMap<>();
    }

    public ExtensibleMapCommandFormat put(String key, Function<Matcher, MapCommand> value) {
        commands.put(Pattern.compile(key, Pattern.CASE_INSENSITIVE), value);
        return this;
    }

    @Override
    public MapCommand fromString(String in) {
        return commands.entrySet().stream()
                .map(e -> new MatchResult(e, in))
                .filter(MatchResult::matches)
                .findAny()
                .map(MatchResult::apply)
                .orElseThrow(() -> new IllegalArgumentException(in));
    }

    @Override
    public String toString(MapCommand cmd) {
        return cmd.asString();
    }

    private static class MatchResult {

        final Matcher matcher;
        final Function<Matcher, MapCommand> conversion;

        public MatchResult(Map.Entry<Pattern, Function<Matcher, MapCommand>> e, String input) {
            this.matcher = e.getKey().matcher(input);
            this.conversion = e.getValue();
        }

        public boolean matches() {
            return matcher.matches();
        }

        public MapCommand apply() {
            return conversion.apply(matcher);
        }

    }

}
