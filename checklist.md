# OTAMS – Deliverable 1 Team Task Distribution (Fall 2025)

## Deliverable 1 Overview
**Goal:** Implement registration, login, and logout functionality for Students and Tutors, display a role-based welcome screen, and optionally use a database for persistent user storage.  
**Deadline:** October 13, 2025  
**Bonus:** +5% for using a database (Firebase or SQLite).

---

## Task Breakdown
| Module | Description | Expected Outcome |
|---------|--------------|------------------|
| 1. Registration | Implement registration screens for Student and Tutor. Capture all required fields and validate inputs. | Student/Tutor data correctly stored and validated. |
| 2. Login & Logout | Implement authentication logic and navigation to the welcome screen. Include a logout option. | User logs in and sees “Welcome! You are logged in as <role>”, can log out successfully. |
| 3. Validation | Implement error checking for all fields (e.g., empty fields, invalid emails, password length). | Each invalid input triggers appropriate error messages. |
| 4. Database (Bonus) | Implement Firebase or SQLite to persist user data (registration, login). | User info remains after app restart (+5% bonus). |
| 5. Documentation & Demo | Prepare UML class diagram, README file (with admin credentials), and a short demo video (≤ 5 min). | All deliverables ready for GitHub release v0.1. |

---

## Team Responsibilities
| Member | Role | Responsibilities |
|---------|------|------------------|
| Sean Jiang | Project Setup & Integration | Initialize Android Studio project and Gradle. Set up navigation between Login → Register → Welcome. Integrate all code. Build final APK and release v0.1. |
| Member B | Registration Lead (UI & Logic) | Implement StudentRegistrationActivity and TutorRegistrationActivity. Design layouts. Handle field input and form submission. Connect with database for saving users. |
| Member C | Authentication Lead (Login & Logout) | Implement LoginActivity and WelcomeActivity. Validate login credentials and identify user role. Implement logout functionality and navigation. |
| Member D | Database & Documentation Lead | Implement Firebase or SQLite integration. Manage CRUD operations for user data. Design UML diagram. Write README (with admin credentials). Record demo video. |

---

## Suggested Timeline
| Week | Tasks |
|------|-------|
| Week 1 (Now – Oct 8) | Project setup complete; Registration & login UI implementation; Database prototype ready |
| Week 2 (Oct 9 – Oct 12) | Integration and testing; Finalize UML & README; Record demo video |
| Oct 13 (Deadline) | Submit GitHub Release v0.1 with APK + UML + README + Video |

---

## Submission Checklist
- [x] GitHub repository includes all team members
- [x] Each member made at least one commit
- [x] Demo video uploaded (≤ 5 min)
- [x] UML class diagram (PDF)
- [x] README file (with Admin credentials)
- [x] APK file uploaded and named properly
- [x] Database implemented (Bonus +5%)

---

### Administrator Account (for testing)
Email: admin@otams.ca
Password: admin123