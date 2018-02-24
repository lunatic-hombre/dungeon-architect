package darch.collection;

import java.util.*;

public class LinkedListHistory<E> implements History<E> {

    private final List<E> list;

    private int index = -1;

    public LinkedListHistory() {
        this(new LinkedList<>());
    }

    public LinkedListHistory(List<E> list) {
        this.list = list;
    }

    @Override
    public E back() {
        if (index <= 0)
            throw new IndexOutOfBoundsException();
        return list.get(--index);
    }

    @Override
    public E forward() {
        if (index >= list.size()-1)
            throw new IndexOutOfBoundsException();
        return list.get(++index);
    }

    @Override
    public void moveTo(E item) {
        final int indexOf = list.indexOf(item);
        if (indexOf > 0)
            index = indexOf;
        else
            add(item);
    }

    // add at position ?
    @Override
    public boolean add(E e) {
        try {
            if (index >= 0 && index < list.size() - 1) {
                list.add(index, e);
                return true;
            }
            return list.add(e);
        } finally {
            index++;
        }
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return list.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }
}
