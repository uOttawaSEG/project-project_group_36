/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Handles user login and role-based navigation for Admin, Tutor, and Student.
 */

package ca.otams.group36.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import ca.otams.group36.R;
import ca.otams.group36.models.Admin;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button buttonLogin;
    private TextView textLoginTitle;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Login");
        }

        // UI elements
        textLoginTitle = findViewById(R.id.textLoginTitle);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        Log.i("Firestore", "Firestore initialized successfully");

        // Login button action
        buttonLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            authenticateUser(email, password);
        });
    }

    /**
     * Authenticate user and handle role-based navigation
     */
    private void authenticateUser(String email, String password) {

        // Admin account
        if (email.equals(Admin.getUsername()) && Admin.authenticate(email, password)) {
            Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Student or Tutor
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "No account found for this email.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String storedPassword = doc.getString("password");
                        String status = doc.getString("status");
                        String role = doc.getString("role");
                        String firstName = doc.getString("firstName");

                        if (storedPassword == null || !storedPassword.equals(password)) {
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (status == null) status = "pending";
                        if (role == null) role = "Student";

                        switch (status) {
                            case "approved":
                                handleApprovedUser(role, firstName, email);
                                break;

                            case "pending":
                                Toast.makeText(this,
                                        "Your account is pending admin approval.",
                                        Toast.LENGTH_LONG).show();
                                break;

                            case "rejected":
                                Toast.makeText(this,
                                        "Your registration was rejected.\nPlease contact admin at 613-000-0000.",
                                        Toast.LENGTH_LONG).show();
                                break;

                            default:
                                Toast.makeText(this,
                                        "Account status unknown. Please contact support.",
                                        Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Redirect approved users to their respective dashboards.
     */
    private void handleApprovedUser(String role, String firstName, String email) {
        Toast.makeText(this, "Welcome " + firstName + " (" + role + ")", Toast.LENGTH_LONG).show();

        Intent intent;

        switch (role.toLowerCase()) {
            case "tutor":
                intent = new Intent(this, TutorDashboardActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("name", firstName);
                break;

            case "student":
                intent = new Intent(this, WelcomeActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("name", firstName);
                break;

            default:
                intent = new Intent(this, WelcomeActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("name", firstName);
                break;
        }

        startActivity(intent);
        finish();
    }


    /**
     * 🔙 Toolbar back button
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
