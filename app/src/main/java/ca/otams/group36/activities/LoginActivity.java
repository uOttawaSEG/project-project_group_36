/**
 * OTAMS Project
 * Author: Lige Xiao
 * University of Ottawa
 *
 * Description:
 * - If credentials match the hard-coded Admin (Admin.authenticate), route directly to Admin UI.
 * - Otherwise, sign in with FirebaseAuth (email + password).
 * - After non-admin sign-in, read or create Firestore profile at users/{uid}.
 * - Only allow access when approved == true.
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

import java.util.HashMap;
import java.util.Map;

import ca.otams.group36.MainActivity;
import ca.otams.group36.R;
import ca.otams.group36.models.Admin;

public class LoginActivity extends AppCompatActivity {

    // UI
    private EditText editEmail, editPassword;
    private Button buttonLogin;

    // Firebase (used for non-admin users)
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Hard-coded admin model (email/password are defined in Admin class)
    private final Admin admin = new Admin();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Toolbar title kept simple to avoid resource dependency issues
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("OTAMS Login");

        // Bind UI
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Firebase init (safe to call; no-op if already initialized)
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Login click
        buttonLogin.setOnClickListener(v -> attemptLogin());
    }

    /**
     * Validates inputs, tries Admin.authenticate first.
     * If not admin, signs in with FirebaseAuth and handles Firestore profile.
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

        // 1) Admin hard-coded login first (no Firebase involved)
        if (admin.authenticate(email, pwd)) {
            Toast.makeText(this, "Admin login successful.", Toast.LENGTH_SHORT).show();

            // Directly navigate to Admin UI; adjust destination if needed
            Intent it = new Intent(this, WelcomeActivity.class);
            it.putExtra("role", "Admin");
            startActivity(it);
            finish();
            return;
        }

        // 2) Non-admin login: FirebaseAuth
        auth.signInWithEmailAndPassword(email, pwd)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    String authedEmail = result.getUser().getEmail() != null
                            ? result.getUser().getEmail() : email;

                    // 2a) Read Firestore user profile
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> handleProfileOrCreate(uid, authedEmail, doc))
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
     * If profile exists, proceed. If missing, create a minimal profile and require approval.
     * Admin path never reaches here (already handled above).
     */
    private void handleProfileOrCreate(String uid, String email, DocumentSnapshot doc) {
        if (!doc.exists()) {
            // Create minimal profile for new non-admin user; requires admin approval.
            Map<String, Object> profile = new HashMap<>();
            profile.put("email", email);
            profile.put("firstName", "User");
            profile.put("lastName", "");
            profile.put("role", "Student");   // Default role for regular users
            profile.put("approved", false);   // Must be flipped by an admin

            db.collection("users").document(uid).set(profile)
                    .addOnSuccessListener(aVoid -> proceedWithProfile(profile))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        setUiEnabled(true);
                    });
            return;
        }

        proceedWithProfile(doc.getData());
    }

    /**
     * Gate by approval and route to destination.
     * Non-admins must have approved == true; otherwise block and sign out.
     */
    private void proceedWithProfile(Map<String, Object> profile) {
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

        // Route by role (adjust if you have specific dashboards)
        if ("Tutor".equalsIgnoreCase(role)) {
            // Example: go to a Tutor home if you have one
            // Intent it = new Intent(this, TutorHomeActivity.class);
            // startActivity(it);
            // For now, fall back to WelcomeActivity:
            Intent it = new Intent(this, WelcomeActivity.class);
            it.putExtra("name", firstName);
            it.putExtra("role", "Tutor");
            startActivity(it);
        } else {
            // Default route for Students (and any other roles)
            Intent it = new Intent(this, WelcomeActivity.class);
            it.putExtra("name", firstName);
            it.putExtra("role", role != null ? role : "Student");
            startActivity(it);
        }
        finish();
    }

    /** Enable/disable inputs during network operations to avoid duplicate actions. */
    private void setUiEnabled(boolean enabled) {
        buttonLogin.setEnabled(enabled);
        editEmail.setEnabled(enabled);
        editPassword.setEnabled(enabled);
    }

    /** Safely read trimmed string from EditText. */
    private static String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /** Safe Boolean cast. */
    private static Boolean asBool(Object o) {
        return (o instanceof Boolean) ? (Boolean) o : null;
    }

    /** Safe String cast. */
    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}