package darch.collection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;
import static org.junit.Assert.assertEquals;

public class ListsTest {

    @Test
    public void add() {
        assertAddPlacement(list(), 3, 0);
        assertAddPlacement(list(1, 2, 3, 4, 5), 3, 3);
        assertAddPlacement(list(1, 2, 4, 5, 6), 3, 2);
        assertAddPlacement(list(1, 2, 4, 5, 6), 7, 5);
    }

    private void assertAddPlacement(List<Integer> list, int expectedValue, int expectedIndex) {
        assertEquals(expectedIndex, Lists.add(list, expectedValue, comparingInt(Integer::intValue)));
        assertEquals(expectedValue, list.get(expectedIndex).intValue());
    }

    private List<Integer> list(Integer... ints) {
        return new ArrayList<>(asList(ints));
    }

}