# OTAMS – Deliverable 2  

## 📱 Overview  

**OTAMS (Online Tutoring Assignment Management System)**  
is an Android application designed to manage student and tutor registration with administrator approval and email verification.  

Building on Deliverable 1, this version introduces:  
- ✅ Email Verification (Firebase Auth)  
- ✅ Admin Approval Workflow (Pending → Approved / Rejected)  
- ✅ Separate Pending and Rejected Request Lists  
- ✅ Firestore Database Integration  
- ✅ Refined UI using uOttawa brand colours  
- ✅ Automatic real-time list refresh and account deletion  

---

## 🔐 Administrator Test Credentials  

| Role | Email | Password |
|------|--------|----------|
| **Admin** | `admin@otams.ca` | `admin123` |

> Use these credentials to log in as **Administrator**.  
> Admin can approve or reject new registrations and permanently delete rejected accounts.

---

## 🧭 How to Verify (Admin Flow)  

1. **Install** the attached APK (`Project_Group_36_v0.2.apk`).  
2. **Login** with the credentials above.  
3. The **Admin Home Screen** shows three buttons:  
   - “View Pending Requests”  
   - “View Rejected Requests”  
   - “Log Out”  
4. Tap **View Pending Requests** → select a user → choose **Approve** or **Reject**.  
   - Approved users can log in after verifying their email.  
   - Rejected users appear under “Rejected Requests”.  
5. In **Rejected Requests**, tapping a user and confirming deletion removes the account from Firestore permanently.  
6. Returning to Admin Home automatically refreshes both lists.

---

## 🧠 User Roles and Permissions  

| Role | Permissions |
|------|--------------|
| **Admin** | Approve / Reject / Delete user accounts |
| **Tutor** | Register with subjects (e.g., “Math, Physics”) → status = pending |
| **Student** | Register with year / program → status = pending |
| **All users** | Must verify email before login |
| **Rejected** | Cannot log in; shown rejection message |

---

## 🧩 System Architecture  

### 🔸 Package Structure
