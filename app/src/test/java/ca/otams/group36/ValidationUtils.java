package ca.otams.group36;

public class ValidationUtils {

    public static boolean isValidName(String name) {
        return name != null && name.matches("^[A-Za-z]{2,}$");
    }

    public static boolean isValidLastName(String lastName) {
        return lastName != null && lastName.matches("^[A-Za-z]{2,}$");
    }

    public static boolean isValidPassword(String password) {
        // Must contain at least one letter and one number, min length 6
        return password != null && password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");
    }
}