package darch.collection;

import java.util.Comparator;
import java.util.List;

public final class Lists {

    /**
     * Assuming the list is sorted, will find appropriate place for item to be placed using binary search.
     * @return the index where the item was placed
     */
    public static <E> int add(List<E> list, E item, Comparator<? super E> comparator) {
        int low = 0;
        int high = list.size() - 1;
        int cmp = -1;

        while (low <= high && cmp != 0) {
            final int mid = (low + high) >>> 1;

            final E midVal = list.get(mid);
            cmp = comparator.compare(midVal, item);

            if (cmp <= 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
        }
        final int index = low;
        list.add(index, item);
        return index;
    }

}
