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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ca.otams.group36.R;

public class TutorRegistrationActivity extends AppCompatActivity {


    private Button btnRegister;
    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword, tilFirstName, tilLastName, tilPhone, tilSubjects, tilYears, tilBio;
    private TextInputEditText etEmail, etPassword, etConfirmPassword, etFirstName, etLastName, etPhone, etSubjects, etYears, etBio;


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

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilPhone = findViewById(R.id.tilPhone);
        tilSubjects = findViewById(R.id.tilSubjects);
        tilYears = findViewById(R.id.tilYears);
        tilBio = findViewById(R.id.tilBio);


        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            if (!validateTutorForm()) {
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
        String subjects = etSubjects.getText().toString().trim();
        String years = etYears.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

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
        user.put("status", "pending");
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

    private boolean validateTutorForm() {
        clearErrors(tilEmail, tilPassword, tilConfirmPassword, tilFirstName, tilLastName, tilPhone, tilSubjects, tilYears, tilBio);

        boolean ok = true;

        String email = safe(etEmail);
        String pwd = safe(etPassword);
        String pwd2 = safe(etConfirmPassword);
        String first = safe(etFirstName);
        String last = safe(etLastName);
        String phone = safe(etPhone);     // 可选
        String subjects = safe(etSubjects);
        String yearsStr = safe(etYears);  // 必填且 >=0
        // bio 可选，不校验

        // 通用：邮箱/密码/确认/名/姓
        if (email.isEmpty()) { tilEmail.setError(getString(R.string.err_required)); ok = false; }
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.err_invalid_email)); ok = false;
        }
        if (pwd.isEmpty()) { tilPassword.setError(getString(R.string.err_required)); ok = false; }
        if (!pwd.isEmpty() && pwd.length() < 8) {
            tilPassword.setError(getString(R.string.err_password_short)); ok = false;
        }
        if (pwd2.isEmpty()) { tilConfirmPassword.setError(getString(R.string.err_required)); ok = false; }
        if (!pwd.isEmpty() && !pwd2.isEmpty() && !pwd.equals(pwd2)) {
            tilConfirmPassword.setError(getString(R.string.err_password_mismatch)); ok = false;
        }
        if (first.isEmpty()) { tilFirstName.setError(getString(R.string.err_required)); ok = false; }
        if (last.isEmpty()) { tilLastName.setError(getString(R.string.err_required)); ok = false; }

        // Subjects：至少一个（逗号分隔时过滤空项）
        if (subjects.isEmpty() || Arrays.stream(subjects.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).count() == 0) {
            tilSubjects.setError(getString(R.string.err_required)); ok = false;
        }

        // Years：必填，非负整数
        if (yearsStr.isEmpty()) {
            tilYears.setError(getString(R.string.err_required)); ok = false;
        } else {
            try {
                int years = Integer.parseInt(yearsStr);
                if (years < 0) { tilYears.setError(getString(R.string.err_nonnegative_number)); ok = false; }
            } catch (NumberFormatException e) {
                tilYears.setError(getString(R.string.err_nonnegative_number)); ok = false;
            }
        }

        // Phone（可选）
        if (!phone.isEmpty() && !phone.matches("^\\+?[0-9\\-() ]{7,}$")) {
            tilPhone.setError(getString(R.string.err_invalid_phone)); ok = false;
        }

        return ok;
    }
    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
    private void clearErrors(TextInputLayout... tills) {
        for (TextInputLayout t : tills) t.setError(null);
    }

}