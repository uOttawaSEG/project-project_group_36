/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Handles user login using Firestore (custom user collection).
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
     * Authenticate user via Firestore document query.
     */
    private void authenticateUser(String email, String password) {
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
                        Boolean approved = doc.getBoolean("approved");
                        String role = doc.getString("role");
                        String firstName = doc.getString("firstName");

                        if (storedPassword != null && storedPassword.equals(password)) {
                            if (approved != null && approved) {
                                Toast.makeText(this,
                                        "Welcome " + firstName + " (" + role + ")",
                                        Toast.LENGTH_LONG).show();

                                Log.i("Login", "User logged in: " + email);

                                Intent intent = new Intent(this, WelcomeActivity.class);
                                intent.putExtra("name", firstName);
                                intent.putExtra("role", role);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this,
                                        "Account pending approval",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Login error", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * 🔙 Handles the top-left return arrow in toolbar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Return to previous screen
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}