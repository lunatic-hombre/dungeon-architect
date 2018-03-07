package darch.map;

public enum RelativeLevel {

    ABOVE(Direction.UP),
    SAME_LEVEL(null),
    BELOW(Direction.DOWN);

    final int scale;
    final Direction direction;

    RelativeLevel(Direction direction) {
        this.direction = direction;
        this.scale = direction == null ? 0 : direction.l;
    }

    public int getScale() {
        return scale;
    }

    public Direction asDirection() {
        return direction;
    }

}
