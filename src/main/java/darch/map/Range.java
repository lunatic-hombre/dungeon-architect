package darch.map;

public interface Range {

    double getStart();

    double getEnd();

    default boolean overlaps(Range range) {
        return includes(range.getStart()) || includes(range.getEnd());
    }

    default boolean includes(double number) {
        return number >= getStart() && number <= getEnd();
    }

    static Range range(double start, double end) {
        return new Range() {
            @Override
            public double getStart() {
                return start;
            }
            @Override
            public double getEnd() {
                return end;
            }
        };
    }

}
