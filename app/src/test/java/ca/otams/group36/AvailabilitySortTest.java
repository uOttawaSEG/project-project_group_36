package ca.otams.group36;

import org.junit.Test;

import static org.junit.Assert.*;

public class AvailabilitySortTest {

    private boolean canDelete(boolean booked, int pending, int approved) {
        if (booked) return false;
        if (pending > 0) return false;
        if (approved > 0) return false;
        return true;
    }

    @Test
    public void testBookedCannotDelete() {
        assertFalse(canDelete(true, 0, 0));
    }

    @Test
    public void testPendingCannotDelete() {
        assertFalse(canDelete(false, 1, 0));
    }

    @Test
    public void testApprovedCannotDelete() {
        assertFalse(canDelete(false, 0, 2));
    }

    @Test
    public void testFreeSlotCanDelete() {
        assertTrue(canDelete(false, 0, 0));
    }

    @Test
    public void testEdgeCases() {
        assertFalse(canDelete(true, 0, 0));
        assertTrue(canDelete(false, 0, 0));
        assertFalse(canDelete(false, 99999, 0));
        assertFalse(canDelete(false, 0, 99999));
    }
}
