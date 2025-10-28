# OTAMS – Deliverable 1 

## Administrator Test Credentials
- Email (username): <admin@otmas.com>
- Password: <admin123>

> Use these credentials to sign in as **Administrator**.

## How to Verify 
1) Install the attached APK (`Project_Group_1_debug.apk`).
2) Login with the above **Administrator** credentials.
3) Expected: Second screen shows  
   `Welcome! You are logged in as Administrator`.

## Build Info
- Release tag: v0.1
- Commit: <short-hash>
- Android: <minSdk/targetSdk/AGP version if helpful>

## Team
Tianqi Jiang – Project Lead (Setup, Integration & Database)

Responsible for overall architecture, Firebase/SQLite integration, and final app assembly.
Worklist

 Initialize Android Studio project and Gradle configuration

 Create base package structure ( activities , models , database , utils )

 Design navigation: LoginActivity → RegisterActivity → WelcomeActivity

 Implement User , Student , Tutor , Admin data model classes

 Set up Firebase Realtime Database connection (or SQLite fallback)

 Implement CRUD operations for registration & login

 Seed Administrator credentials ( admin@otams.ca / admin123 )

 Integrate UI and logic from other members

 Conduct full functionality test (register → login → logout)

 Build final APK and create GitHub Release (v0.1)

FuFeng Zhong – Registration Lead 

Responsible for the design and logic of Student/Tutor registration screens.
Worklist

 Create StudentRegistrationActivity layout (XML + Kotlin/Java)

 Create TutorRegistrationActivity layout (XML + Kotlin/Java)

Add text input fields and buttons for all required attributes

 Implement input validation: empty fields, email format, password length

 Display user-friendly error messages for invalid inputs

 Connect registration data to database functions (provided by Tianqi)

Test both Student and Tutor registration workflows

 Capture screenshots for README and demo video

Lige Xiao – Authentication & Navigation Lead

Responsible for login, logout, and role-based navigation.

Worklist

 Implement LoginActivity with email/password fields

 Verify credentials using database query (Student, Tutor, Admin)

 Navigate to WelcomeActivity after successful login

 Display correct message: “Welcome! You are logged in as <role>”

Add Logout button to clear session and return to login screen

 Handle error messages for incorrect credentials

 Perform cross-role testing (Student / Tutor / Admin accounts)

Romy Wang – Documentation & Demo Lead

Responsible for UML, README, and demo video preparation.
Worklist

 Design UML Class Diagram ( User , Student , Tutor , Admin , relationships)

 Ensure UML includes attributes and associations (no Activity classes)

 Write README.md including:

 Project overview and installation instructions

Admin credentials for testing

Team roles and contributions

 Record ≤ 5-minute demo video showing registration → login → logout

 Verify submission includes: APK, UML PDF, README, and video

 Confirm correct file names and GitHub Release version (v0.1)
