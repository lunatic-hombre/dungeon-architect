package mapgen.map;

import java.util.Optional;

public interface RoomPointer extends WallPointer {

    // TODO
    Optional<Integer> getChildIndex();

    RelativeLevel getLevel();

}
