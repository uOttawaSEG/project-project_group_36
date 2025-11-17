package ca.otams.group36;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationUnitTest {

    @Test
    public void validateName_isCorrect() {
        assertTrue(ValidationUtils.isValidName("Alice"));
        assertFalse(ValidationUtils.isValidName("A"));         // too short
        assertFalse(ValidationUtils.isValidName("1234"));      // digits
        assertFalse(ValidationUtils.isValidName("John@Doe"));  // symbols
    }

    @Test
    public void validateLastName_isCorrect() {
        assertTrue(ValidationUtils.isValidLastName("Johnson"));
        assertFalse(ValidationUtils.isValidLastName(""));       // empty
        assertFalse(ValidationUtils.isValidLastName("A1"));     // invalid chars
    }

    @Test
    public void validatePassword_isCorrect() {
        assertTrue(ValidationUtils.isValidPassword("Abcd1234"));
        assertFalse(ValidationUtils.isValidPassword("123"));          // too short
        assertFalse(ValidationUtils.isValidPassword("password"));     // no digits
        assertFalse(ValidationUtils.isValidPassword("12345678"));     // no letters
    }
}