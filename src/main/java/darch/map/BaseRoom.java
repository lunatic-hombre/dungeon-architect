package darch.map;

public class BaseRoom implements Room {

    private final int depth, length;

    public BaseRoom(int depth, int length) {
        this.depth = depth;
        this.length = length;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public int getLength() {
        return length;
    }

}
