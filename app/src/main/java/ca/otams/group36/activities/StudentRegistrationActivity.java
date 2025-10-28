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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.otams.group36.R;

public class StudentRegistrationActivity extends AppCompatActivity {

    private Button btnRegister;
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

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);

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
            // 通过校验 -> 继续创建账号 / 写库
            handleRegister();
        });

    }

    private void handleRegister() {
        String email = etEmail.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Generate custom UID (manual system)
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

    private boolean validateStudentForm() {
        clearErrors(tilEmail, tilPassword, tilConfirmPassword, tilFirstName, tilLastName, tilPhone);

        boolean ok = true;

        String email = safe( etEmail);
        String pwd = safe( etPassword);
        String pwd2 = safe( etConfirmPassword);
        String first = safe( etFirstName);
        String last = safe( etLastName);
        String phone = safe( etPhone); // 可选

        // 必填：Email/Password/Confirm/First/Last
        if (email.isEmpty()) { tilEmail.setError(getString(R.string.err_required)); ok = false; }
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.err_invalid_email)); ok = false;
        }
        if (pwd.isEmpty()) { tilPassword.setError(getString(R.string.err_required)); ok = false; }
        if (!pwd.isEmpty() && pwd.length() < 8) { // 也可换成 6：用 err_password_min6
            tilPassword.setError(getString(R.string.err_password_short)); ok = false;
        }
        if (pwd2.isEmpty()) { tilConfirmPassword.setError(getString(R.string.err_required)); ok = false; }
        if (!pwd.isEmpty() && !pwd2.isEmpty() && !pwd.equals(pwd2)) {
            tilConfirmPassword.setError(getString(R.string.err_password_mismatch)); ok = false;
        }
        if (first.isEmpty()) { tilFirstName.setError(getString(R.string.err_required)); ok = false; }
        if (last.isEmpty()) { tilLastName.setError(getString(R.string.err_required)); ok = false; }

        // Phone（可选）：若填写则做格式检查
        if (!phone.isEmpty()) {
            // 宽松校验：可带 + - () 空格，长度>=7
            if (!phone.matches("^\\+?[0-9\\-() ]{7,}$")) {
                tilPhone.setError(getString(R.string.err_invalid_phone)); ok = false;
            }
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