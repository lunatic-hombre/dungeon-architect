package darch.map;

import java.util.Optional;

public interface WallPointer {

    Optional<CardinalPoint> getDirection();

    Optional<Integer> getIndex();

}
