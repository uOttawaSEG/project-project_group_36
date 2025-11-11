/**
 * OTAMS Project - Deliverable 3
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Model class representing a tutoring session between a Student and a Tutor.
 */
package ca.otams.group36.models;

import com.google.firebase.Timestamp; // ← added

public class Session {

    // Firestore auto-generated document ID (used locally)
    private String id;

    private String slotId;         // Reference to Availability document
    private String tutorEmail;
    private String tutorName;
    private String studentEmail;
    private String studentName;
    private String subject;
    private String date;
    private String startTime;
    private String endTime;
    private String status;         // "pending", "approved", "rejected", "cancelled"

    // ← added: canonical start time for time-based queries (Upcoming/Past)
    private Timestamp startAt;

    public Session() {}

    public Session(String slotId, String tutorEmail, String tutorName,
                   String studentEmail, String studentName, String subject,
                   String date, String startTime, String endTime, String status) {
        this.slotId = slotId;
        this.tutorEmail = tutorEmail;
        this.tutorName = tutorName;
        this.studentEmail = studentEmail;
        this.studentName = studentName;
        this.subject = subject;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        // NOTE: startAt is set via setter to keep changes minimal.
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getTutorEmail() { return tutorEmail; }
    public void setTutorEmail(String tutorEmail) { this.tutorEmail = tutorEmail; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ← added
    public Timestamp getStartAt() { return startAt; }
    public void setStartAt(Timestamp startAt) { this.startAt = startAt; }

    @Override
    public String toString() {
        return subject + " (" + date + " " + startTime + "-" + endTime + ") [" + status + "]";
    }
}
