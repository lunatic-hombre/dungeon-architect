package mapgen.map;

import java.util.Optional;

public interface WallPointer {

    Optional<CardinalDirection> getDirection();

    Optional<Integer> getIndex();

}
