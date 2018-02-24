package darch.map;

public class RelativeRoomLocation {

    final CardinalPoint direction;
    final int parentIndex, childIndex;
    final RelativeLevel floor;

    public RelativeRoomLocation(CardinalPoint direction, int parentIndex, int childIndex, RelativeLevel floor) {
        this.direction = direction;
        this.parentIndex = parentIndex;
        this.childIndex = childIndex;
        this.floor = floor;
    }

    public CardinalPoint getDirection() {
        return direction;
    }

    public int getParentIndex() {
        return parentIndex;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public RelativeLevel getFloor() {
        return floor;
    }

}
