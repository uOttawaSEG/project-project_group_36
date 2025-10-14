/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Handles student registration and Firestore record creation.
 */

package ca.otams.group36.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.otams.group36.R;

public class StudentRegistrationActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword, etFirstName, etLastName, etPhone;
    private Button btnRegister;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Register as Student");
        }

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(view -> handleRegister());
    }

    private void handleRegister() {
        String email = etEmail.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validation
        if (email.isEmpty() || pwd.isEmpty() || confirm.isEmpty() ||
                firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pwd.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate custom UID (manual system)
        String uid = String.format("%08d", new java.util.Random().nextInt(100000000));

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", pwd);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("phone", phone);
        user.put("approved", false);
        user.put("role", "Student");

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student registered successfully! Pending approval.", Toast.LENGTH_SHORT).show();
                    finish(); // Return after success
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * 🔙 Handle back button in toolbar
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