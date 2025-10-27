/**
 * OTAMS Project 
 * Author: Tianqi Jiang
 * University of Ottawa

 * Description:
 * Represents the predefined Administrator account
 * responsible for approving or rejecting registration requests.
 */

package ca.otams.group36.models;

public class Admin {
    private static final String username = "admin@otams.ca";

    public static String getUsername() { return username; }

    public static boolean authenticate(String email, String pwd) {
        String password = "admin123";
        return email.equals(username) && pwd.equals(password);
    }
}