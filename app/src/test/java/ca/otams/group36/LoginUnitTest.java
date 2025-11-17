package ca.otams.group36;

import org.junit.Test;
import static org.junit.Assert.*;

public class LoginUnitTest {

    @Test
    public void testFirstName() {
        String name = "user1";
        assertNotEquals("user", name);
    }

    @Test
    public void testLastName() {
        String last = "testLast";
        assertNotEquals("last", last);
    }

    @Test
    public void testPassword() {
        String pwd = "123456";
        assertTrue(pwd.length() >= 6);
    }
}
