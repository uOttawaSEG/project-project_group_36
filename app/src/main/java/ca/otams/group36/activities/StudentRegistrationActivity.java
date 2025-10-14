/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Student registration screen with local validation.
 * Writes directly to Firestore with random uid (no Auth).
 */

package ca.otams.group36.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword, tilFirstName, tilLastName, tilPhone;
    private TextInputEditText etEmail, etPassword, etConfirmPassword, etFirstName, etLastName, etPhone;
    private Button btnRegister;
    private ProgressBar progress;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Register");
        }

        // Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Bind views (ids match XML)
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilPhone = findViewById(R.id.tilPhone);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);

        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);

        setupErrorClear();

        btnRegister.setOnClickListener(v -> {
            if (!validateInputs()) {
                Toast.makeText(this, "Please fix the highlighted errors", Toast.LENGTH_SHORT).show();
                return;
            }
            doRegisterDirectFirestore();
        });
    }

    /** 输入变化就清除对应错误（空指针安全） */
    private void setupErrorClear() {
        attachErrorCleaner(tilEmail);
        attachErrorCleaner(tilPassword);
        attachErrorCleaner(tilConfirmPassword);
        attachErrorCleaner(tilFirstName);
        attachErrorCleaner(tilLastName);
        attachErrorCleaner(tilPhone);
    }

    private void attachErrorCleaner(TextInputLayout til) {
        if (til == null) return;
        TextInputEditText et = (TextInputEditText) til.getEditText();
        if (et == null) return;
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { til.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /** 表单校验：必填、邮箱格式、密码≥6、确认密码一致、电话 10~15 位（允许+空格-括号） */
    private boolean validateInputs() {
        clearErrors();

        String email = getText(etEmail);
        String pwd = getText(etPassword);
        String confirm = getText(etConfirmPassword);
        String firstName = getText(etFirstName);
        String lastName = getText(etLastName);
        String phone = getText(etPhone);

        boolean ok = true;

        if (firstName.isEmpty()) { tilFirstName.setError("First name is required"); ok = false; }
        if (lastName.isEmpty())  { tilLastName.setError("Last name is required");  ok = false; }

        if (email.isEmpty()) {
            tilEmail.setError("Email is required"); ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email format"); ok = false;
        }

        if (pwd.isEmpty()) {
            tilPassword.setError("Password is required"); ok = false;
        } else if (pwd.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters"); ok = false;
        }

        if (confirm.isEmpty()) {
            tilConfirmPassword.setError("Please confirm your password"); ok = false;
        } else if (!confirm.equals(pwd)) {
            tilConfirmPassword.setError("Passwords do not match"); ok = false;
        }

        if (phone.isEmpty()) {
            tilPhone.setError("Phone is required"); ok = false;
        } else if (!isValidPhone(phone)) {
            tilPhone.setError("Invalid phone number"); ok = false;
        }

        return ok;
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilPhone.setError(null);
    }

    private String getText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private boolean isValidPhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        return digits.length() >= 10 && digits.length() <= 15
                && phone.matches("^\\+?[0-9 ()-]{7,20}$");
    }

    /** 不走 Auth：随机 uid 直接写 Firestore 的原逻辑 */
    private void doRegisterDirectFirestore() {
        showProgress(true);

        String email = getText(etEmail);
        String pwd = getText(etPassword);
        String firstName = getText(etFirstName);
        String lastName = getText(etLastName);
        String phone = getText(etPhone);

        String uid = String.format("%08d", new java.util.Random().nextInt(100000000));

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", pwd);   // 按你的原结构保留
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("phone", phone);
        user.put("approved", false);
        user.put("role", "Student");

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showProgress(boolean show) {
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
        if (btnRegister != null) btnRegister.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
