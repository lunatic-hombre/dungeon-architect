package darch.map;

public interface Range {

    double getStart();

    double getEnd();

    default boolean overlaps(Range range) {
        return includes(range.getStart()) || includes(range.getEnd())
                || range.includes(this.getStart()) || range.includes(this.getEnd());
    }

    default boolean includes(double number) {
        return number >= getStart() && number <= getEnd();
    }

    static Range exclusive(double start, double end) {
        return inclusive(start + 0.001, end - 0.001);
    }

    static Range inclusive(double start, double end) {
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
