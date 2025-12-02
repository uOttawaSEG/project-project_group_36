package ca.otams.group36;

import org.junit.Test;
import static org.junit.Assert.*;

public class TimeOverlapTest {

    private boolean overlap(int s1, int e1, int s2, int e2) {
        return (s1 < e2) && (s2 < e1);
    }

    @Test
    public void testPartialOverlap() {
        assertTrue(overlap(540, 600, 570, 630));
    }

    @Test
    public void testExactTouch_NoOverlap() {
        assertFalse(overlap(540, 600, 600, 660));
    }

    @Test
    public void testExactMatch_Overlap() {
        assertTrue(overlap(540, 600, 540, 600));
    }

    @Test
    public void testSlotInsideAnother() {
        assertTrue(overlap(540, 600, 550, 560));
    }

    @Test
    public void testNoOverlapCompletelyBefore() {
        assertFalse(overlap(300, 400, 500, 600));
    }

    @Test
    public void testNoOverlapCompletelyAfter() {
        assertFalse(overlap(700, 800, 500, 600));
    }

    @Test
    public void testNegativeValues_NoOverlap() {
        assertFalse(overlap(-100, 0, 10, 20));
    }

    @Test
    public void testZeroLengthIntervals() {
        assertFalse(overlap(500, 500, 500, 500));
    }
}
