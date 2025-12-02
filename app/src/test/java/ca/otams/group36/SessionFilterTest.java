package ca.otams.group36;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SessionFilterTest {

    private boolean isPast(Timestamp startAt, Timestamp now) {
        if (startAt == null) return false;
        return startAt.compareTo(now) < 0;
    }

    private boolean isUpcoming(Timestamp startAt, Timestamp now) {
        if (startAt == null) return false;
        return startAt.compareTo(now) >= 0;
    }

    @Test
    public void testPast() {
        Timestamp past = new Timestamp(new Date(1700000000000L));
        assertTrue(isPast(past, Timestamp.now()));
    }

    @Test
    public void testFuture() {
        Timestamp future = new Timestamp(new Date(System.currentTimeMillis() + 50000000));
        assertTrue(isUpcoming(future, Timestamp.now()));
    }

    @Test
    public void testNowCountsAsUpcoming() {
        Timestamp now = Timestamp.now();
        assertTrue(isUpcoming(now, now));
    }

    @Test
    public void testNullTimestamp() {
        assertFalse(isPast(null, Timestamp.now()));
        assertFalse(isUpcoming(null, Timestamp.now()));
    }

    @Test
    public void testSorting() {
        List<Timestamp> list = new ArrayList<>();
        list.add(new Timestamp(new Date(1700006000000L)));
        list.add(new Timestamp(new Date(1700001000000L)));
        list.add(new Timestamp(new Date(1700009000000L)));

        list.sort(Comparator.comparing(Timestamp::toDate));

        assertEquals(1700001000000L, list.get(0).toDate().getTime());
        assertEquals(1700006000000L, list.get(1).toDate().getTime());
        assertEquals(1700009000000L, list.get(2).toDate().getTime());
    }
}
