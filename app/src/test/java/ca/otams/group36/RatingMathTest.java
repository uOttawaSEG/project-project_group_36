package ca.otams.group36;

import org.junit.Test;
import java.util.Random;

import static org.junit.Assert.*;

public class RatingMathTest {

    private double calc(long sum, long count) {
        return count == 0 ? 0 : (double) sum / count;
    }

    @Test
    public void testZeroReviews() {
        assertEquals(0.0, calc(0, 0), 0.0001);
    }

    @Test
    public void testSimpleAverage() {
        assertEquals(4.5, calc(9, 2), 0.0001);
    }

    @Test
    public void testLargeSum() {
        assertEquals(4.0, calc(40000, 10000), 0.0001);
    }

    @Test
    public void testFloatPrecision() {
        assertEquals(3.3333, calc(10, 3), 0.001);
    }

    @Test
    public void testRandomRatings() {
        Random r = new Random(0);
        for (int i = 0; i < 100; i++) {
            long sum = r.nextInt(500);
            long count = r.nextInt(20) + 1;
            assertEquals((double) sum / count, calc(sum, count), 0.0001);
        }
    }

    @Test
    public void testMaxValue() {
        double expected = (double) Long.MAX_VALUE / 5;
        assertEquals(expected, calc(Long.MAX_VALUE, 5), 0.0001);
    }

    @Test
    public void testNegativeRating() {
        assertEquals(-2.0, calc(-10, 5), 0.0001);
    }
}
