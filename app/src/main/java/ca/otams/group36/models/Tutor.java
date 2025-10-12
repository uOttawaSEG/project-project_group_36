/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa

 * Description:
 * Tutor subclass extending User
 */

package ca.otams.group36.models;

import java.util.List;

public class Tutor extends User {
    private String highestDegree;
    private List<String> coursesOffered;

    public Tutor() { super(); }

    public Tutor(String firstName, String lastName, String email,
                 String password, String phone, String highestDegree,
                 List<String> coursesOffered) {
        super(firstName, lastName, email, password, phone, "Tutor", false);
        this.highestDegree = highestDegree;
        this.coursesOffered = coursesOffered;
    }

    public String getHighestDegree() { return highestDegree; }
    public void setHighestDegree(String highestDegree) { this.highestDegree = highestDegree; }

    public List<String> getCoursesOffered() { return coursesOffered; }
    public void setCoursesOffered(List<String> coursesOffered) { this.coursesOffered = coursesOffered; }
}