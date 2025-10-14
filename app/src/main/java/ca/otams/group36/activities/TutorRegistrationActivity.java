/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Handles tutor registration and Firestore record creation.
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

public class TutorRegistrationActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword,
            etFirstName, etLastName, etPhone, etSubjects, etYears, etBio;
    private Button btnRegister;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_registration);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Register as Tutor");
        }

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etSubjects = findViewById(R.id.etSubjects);
        etYears = findViewById(R.id.etYears);
        etBio = findViewById(R.id.etBio);
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
        String subjects = etSubjects.getText().toString().trim();
        String years = etYears.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (email.isEmpty() || pwd.isEmpty() || confirm.isEmpty()
                || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pwd.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = String.format("%08d", new java.util.Random().nextInt(100000000));

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", pwd);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("phone", phone);
        user.put("subjects", subjects);
        user.put("years", years);
        user.put("bio", bio);
        user.put("approved", false);
        user.put("role", "Tutor");

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tutor registered successfully! Pending approval.", Toast.LENGTH_SHORT).show();
                    finish(); // return after success
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * 🔙 Handle toolbar back button
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