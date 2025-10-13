/**
 * OTAMS Project
 * Author: (updated)
 *
 * Description:
 * - Sign in with FirebaseAuth (email + password)
 * - Read Firestore profile at users/{uid}
 * - If profile is missing, auto-create it (admins via whitelist -> approved=true)
 * - Only allow navigation if approved == true
 * - Admins go to MainActivity; others go to WelcomeActivity
 */

package ca.otams.group36.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.otams.group36.MainActivity;
import ca.otams.group36.R;

public class LoginActivity extends AppCompatActivity {

    // UI refs (IDs must match your activity_login.xml)
    private EditText editEmail, editPassword;
    private Button buttonLogin;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Admin email whitelist: add all admin emails here
    private static final Set<String> ADMIN_WHITELIST = new HashSet<>(
            Arrays.asList("admin@otams.ca")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Toolbar (kept simple to avoid resource mismatches)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("OTAMS Login");
        }

        // Bind UI
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Firebase init
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Click: attempt login
        buttonLogin.setOnClickListener(v -> attemptLogin());
    }

    /**
     * Validate inputs, then sign in via FirebaseAuth.
     * On success, fetch or create the Firestore profile and route by role.
     */
    private void attemptLogin() {
        String email = safe(editEmail);
        String pwd   = safe(editPassword);

        // Basic validation
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_LONG).show();
            return;
        }
        if (pwd.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters.", Toast.LENGTH_LONG).show();
            return;
        }

        setUiEnabled(false);

        // 1) Sign in with FirebaseAuth (never compare plaintext passwords in Firestore)
        auth.signInWithEmailAndPassword(email, pwd)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    String authedEmail = result.getUser().getEmail() != null
                            ? result.getUser().getEmail()
                            : email;

                    // 2) Read Firestore profile at users/{uid}
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> handleProfileOrBootstrap(uid, authedEmail, doc))
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_LONG).show();
                                setUiEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "User not found.", Toast.LENGTH_LONG).show();
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid credentials.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_LONG).show();
                    }
                    setUiEnabled(true);
                });
    }

    /**
     * If profile exists, proceed. If missing, auto-create users/{uid}.
     * Admin emails (in whitelist) are auto-approved.
     */
    private void handleProfileOrBootstrap(String uid, String email, DocumentSnapshot doc) {
        if (!doc.exists()) {
            boolean isAdmin = ADMIN_WHITELIST.contains(email.toLowerCase());

            Map<String, Object> profile = new HashMap<>();
            profile.put("email", email);
            profile.put("firstName", isAdmin ? "Admin" : "User");
            profile.put("lastName", "");
            profile.put("role", isAdmin ? "Admin" : "Student"); // Non-admin defaults to Student
            profile.put("approved", isAdmin);                    // Admins are auto-approved; others require approval

            db.collection("users").document(uid).set(profile)
                    .addOnSuccessListener(aVoid -> proceedWithProfile(uid, profile))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        setUiEnabled(true);
                    });
            return;
        }

        proceedWithProfile(uid, doc.getData());
    }

    /**
     * Gate access by approval, then route by role.
     */
    private void proceedWithProfile(String uid, Map<String, Object> profile) {
        if (profile == null) {
            Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_LONG).show();
            setUiEnabled(true);
            return;
        }

        Boolean approved = asBool(profile.get("approved"));
        String role = asString(profile.get("role"));
        String firstName = asString(profile.get("firstName"));

        if (approved == null || !approved) {
            Toast.makeText(this, "Your account is pending admin approval.", Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut();
            setUiEnabled(true);
            return;
        }

        Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show();

        // Route by role (adjust destinations to your app structure)
        if ("Admin".equalsIgnoreCase(role)) {
            Intent it = new Intent(this, MainActivity.class);  // Replace with AdminDashboardActivity if you have one
            it.putExtra("role", "Admin");
            startActivity(it);
        } else {
            Intent it = new Intent(this, WelcomeActivity.class);
            it.putExtra("name", firstName);
            it.putExtra("role", role != null ? role : "Student");
            startActivity(it);
        }
        finish();
    }

    /**
     * Enable/disable inputs while network operations are in progress.
     */
    private void setUiEnabled(boolean enabled) {
        buttonLogin.setEnabled(enabled);
        editEmail.setEnabled(enabled);
        editPassword.setEnabled(enabled);
    }

    /** Safe string from EditText (trimmed) */
    private static String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /** Safe boolean cast */
    private static Boolean asBool(Object o) {
        return (o instanceof Boolean) ? (Boolean) o : null;
    }

    /** Safe string cast */
    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
