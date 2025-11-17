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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.otams.group36.R;

public class StudentRegistrationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword, tilFirstName, tilLastName, tilPhone;
    private TextInputEditText etEmail, etPassword, etConfirmPassword, etFirstName, etLastName, etPhone;

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

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilPhone = findViewById(R.id.tilPhone);

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            if (!validateStudentForm()) {
                Toast.makeText(this, getString(R.string.please_fix_errors), Toast.LENGTH_SHORT).show();
                return;
            }
            handleRegister();
        });
    }

    /**
     * Handle Firestore registration
     */
    private void handleRegister() {
        String email = etEmail.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Generate manual UID
        String uid = String.format("%08d", new java.util.Random().nextInt(100000000));

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", pwd);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("phone", phone);
        user.put("status", "pending");
        user.put("role", "Student");

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student registered successfully! Pending approval.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Toolbar back button handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Validate all student form fields
     */
    private boolean validateStudentForm() {
        clearErrors(tilEmail, tilPassword, tilConfirmPassword, tilFirstName, tilLastName, tilPhone);

        boolean ok = true;

        String email = safe(etEmail);
        String pwd = safe(etPassword);
        String pwd2 = safe(etConfirmPassword);
        String first = safe(etFirstName);
        String last = safe(etLastName);
        String phone = safe(etPhone);

        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.err_required));
            ok = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.err_invalid_email));
            ok = false;
        }

        if (pwd.isEmpty()) {
            tilPassword.setError(getString(R.string.err_required));
            ok = false;
        } else if (pwd.length() < 8) {
            tilPassword.setError(getString(R.string.err_password_short));
            ok = false;
        }

        if (pwd2.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.err_required));
            ok = false;
        } else if (!pwd.equals(pwd2)) {
            tilConfirmPassword.setError(getString(R.string.err_password_mismatch));
            ok = false;
        }

        if (first.isEmpty()) {
            tilFirstName.setError(getString(R.string.err_required));
            ok = false;
        } else if (!first.matches("^[A-Za-z]{2,}$")) {
            tilFirstName.setError("Name is invalid");
            ok = false;
        }

        if (last.isEmpty()) {
            tilLastName.setError(getString(R.string.err_required));
            ok = false;
        } else if (!last.matches("^[A-Za-z]{2,}$")) {
            tilLastName.setError("Last name is invalid");
            ok = false;
        }

        if (!phone.isEmpty() && !phone.matches("^\\+?[0-9\\-() ]{7,}$")) {
            tilPhone.setError(getString(R.string.err_invalid_phone));
            ok = false;
        }

        return ok;
    }

    private void clearErrors(TextInputLayout... tills) {
        for (TextInputLayout t : tills) t.setError(null);
    }

    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}