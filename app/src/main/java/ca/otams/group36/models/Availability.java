/**
 * OTAMS Project - Deliverable 3
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Model class representing a Tutor's availability slot.
 */

package ca.otams.group36.models;

public class Availability {

    // Firestore auto-generated ID (not stored in DB, used locally)
    private String id;

    private String tutorEmail;
    private String tutorName;
    private String subject;       // optional - used for future Deliverable 4
    private String date;
    private String startTime;
    private String endTime;
    private boolean autoApprove;
    private boolean isBooked;

    public Availability() {}

    public Availability(String tutorEmail, String tutorName, String subject,
                        String date, String startTime, String endTime,
                        boolean autoApprove) {
        this.tutorEmail = tutorEmail;
        this.tutorName = tutorName;
        this.subject = subject;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.autoApprove = autoApprove;
        this.isBooked = false;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTutorEmail() { return tutorEmail; }
    public void setTutorEmail(String tutorEmail) { this.tutorEmail = tutorEmail; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public boolean isAutoApprove() { return autoApprove; }
    public void setAutoApprove(boolean autoApprove) { this.autoApprove = autoApprove; }

    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }

    @Override
    public String toString() {
        return subject + " | " + date + " " + startTime + "-" + endTime;
    }
}
