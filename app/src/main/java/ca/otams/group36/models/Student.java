/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa

 * Description:
 * Student subclass extending User
 */

package ca.otams.group36.models;

public class Student extends User {

    public Student() { super(); }

    public Student(String firstName, String lastName, String email,
                   String password, String phone, String programOfStudy) {
        super(firstName, lastName, email, password, phone, "Student", false);
    }
}