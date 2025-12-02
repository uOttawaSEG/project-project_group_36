package ca.otams.group36.models;

import com.google.firebase.Timestamp;

public class Availability {

    private String id;
    private String tutorEmail;
    private String tutorName;

    private String courseCode;
    private String subject;
    private boolean autoApprove;

    private String date;
    private String startTime;
    private String endTime;

    private int startMinutes;
    private int endMinutes;

    private Timestamp startAt;
    private Timestamp endAt;

    private boolean booked;
    private Timestamp createdAt;

    public Availability() {}

    public Availability(String tutorEmail, String tutorName,
                        String courseCode, String subject,
                        String date, String startTime, String endTime,
                        int startMinutes, int endMinutes,
                        Timestamp startAt, Timestamp endAt,
                        boolean autoApprove, boolean booked) {

        this.tutorEmail = tutorEmail;
        this.tutorName = tutorName;
        this.courseCode = courseCode;
        this.subject = subject;

        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;

        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;

        this.startAt = startAt;
        this.endAt = endAt;

        this.autoApprove = autoApprove;
        this.booked = booked;

        this.createdAt = Timestamp.now();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTutorEmail() { return tutorEmail; }
    public void setTutorEmail(String tutorEmail) { this.tutorEmail = tutorEmail; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public boolean isAutoApprove() { return autoApprove; }
    public void setAutoApprove(boolean autoApprove) { this.autoApprove = autoApprove; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getStartMinutes() { return startMinutes; }
    public void setStartMinutes(int startMinutes) { this.startMinutes = startMinutes; }

    public int getEndMinutes() { return endMinutes; }
    public void setEndMinutes(int endMinutes) { this.endMinutes = endMinutes; }

    public Timestamp getStartAt() { return startAt; }
    public void setStartAt(Timestamp startAt) { this.startAt = startAt; }

    public Timestamp getEndAt() { return endAt; }
    public void setEndAt(Timestamp endAt) { this.endAt = endAt; }

    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
