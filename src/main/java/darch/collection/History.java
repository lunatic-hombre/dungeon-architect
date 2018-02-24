package darch.collection;

import java.util.Collection;

public interface History<E> extends Collection<E> {

    /**
     * Returns previous element and maintains new position.
     * @return the previous element.
     * @throws IndexOutOfBoundsException when there is nothing behind.
     */
    E back();

    /**
     * Returns next element and maintains new position.
     * @return the next element.
     * @throws IndexOutOfBoundsException when there is nothing ahead.
     */
    E forward();

    /**
     * Sets position to the specified element. If the element does not exist, it will add it and advance.
     * @param item
     */
    void moveTo(E item);

}
