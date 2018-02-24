package darch.cmd;

public interface Format<T> {

    T fromString(String str);

    String toString(T value);

}
