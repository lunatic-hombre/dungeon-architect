package mapgen.map;

public enum RelativeLevel {

    ABOVE(1),
    SAME_LEVEL(0),
    BELOW(-1);

    final int scale;

    RelativeLevel(int scale) {
        this.scale = scale;
    }

    public int getScale() {
        return scale;
    }

}
