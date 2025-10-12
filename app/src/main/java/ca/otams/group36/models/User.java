/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa

 * Description:
 * Base user class for all roles (Student, Tutor, Admin)
 */

package ca.otams.group36.models;

public class User {
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String password;
    protected String phone;
    protected String role;
    protected boolean approved;

    public User() {}

    public User(String firstName, String lastName, String email,
                String password, String phone, String role, boolean approved) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.approved = approved;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}