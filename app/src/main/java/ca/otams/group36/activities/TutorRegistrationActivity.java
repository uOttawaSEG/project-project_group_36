/**
 * OTAMS Project
 * Author: Yufeng zhong
 * University of Ottawa
 *
 * Description:
 * Handles tutor registration and Firestore record creation.
 */

package ca.otams.group36.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
    private ProgressBar progress; // 可选：如果布局里有就会控制

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

        // Match IDs from XML
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
        progress = findViewById(R.id.progress); // 可能为 null，代码里已做保护

        btnRegister.setOnClickListener(view -> handleRegister());
    }

    private void handleRegister() {
        // 读取输入
        String email = t(etEmail);
        String pwd = t(etPassword);
        String confirm = t(etConfirmPassword);
        String firstName = t(etFirstName);
        String lastName = t(etLastName);
        String phone = t(etPhone);
        String subjects = t(etSubjects);
        String yearsStr = t(etYears);
        String bio = t(etBio);

        // 表单校验
        clearErrors();
        boolean ok = true;

        if (firstName.isEmpty()) { setErr(etFirstName, "First name is required"); ok = false; }
        if (lastName.isEmpty())  { setErr(etLastName,  "Last name is required");  ok = false; }

        if (email.isEmpty()) {
            setErr(etEmail, "Email is required"); ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setErr(etEmail, "Please enter a valid email"); ok = false;
        }

        if (pwd.isEmpty()) {
            setErr(etPassword, "Password is required"); ok = false;
        } else if (pwd.length() < 6) {
            setErr(etPassword, "Password must be at least 6 characters"); ok = false;
        }

        if (confirm.isEmpty()) {
            setErr(etConfirmPassword, "Please confirm your password"); ok = false;
        } else if (!pwd.equals(confirm)) {
            setErr(etConfirmPassword, "Passwords do not match"); ok = false;
        }

        // 你的旧代码把 phone 设为必填；保持一致（如果想改为可选，把这一段的 isEmpty 判断去掉即可）
        if (phone.isEmpty()) {
            setErr(etPhone, "Phone is required"); ok = false;
        } else if (!isValidPhone(phone)) {
            setErr(etPhone, "Invalid phone number"); ok = false;
        }

        if (subjects.isEmpty()) {
            setErr(etSubjects, "Please enter at least one subject"); ok = false;
        }

        Integer yearsInt = null;
        try { yearsInt = yearsStr.isEmpty() ? null : Integer.valueOf(yearsStr); }
        catch (NumberFormatException ignore) { yearsInt = null; }
        if (yearsInt == null || yearsInt < 0) {
            setErr(etYears, "Please enter a valid non-negative number"); ok = false;
        }

        if (!ok) {
            Toast.makeText(this, "Please fix the highlighted errors", Toast.LENGTH_SHORT).show();
            return;
        }

        // === 不改你的接口：随机 uid + Firestore.set(...)，字段名保持一致 ===
        setLoading(true);

        String uid = String.format("%08d", new java.util.Random().nextInt(100000000));

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", pwd);      // 保留你的原字段（注意生产上不建议存明文）
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("phone", phone);
        user.put("subjects", subjects); // 按你的原逻辑存字符串
        user.put("years", yearsStr);    // 按你的原逻辑存字符串
        user.put("bio", bio);
        user.put("approved", false);
        user.put("role", "Tutor");

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(this, "Tutor registered successfully! Pending approval.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // --- helpers ---

    private void clearErrors() {
        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);
        etFirstName.setError(null);
        etLastName.setError(null);
        etPhone.setError(null);
        etSubjects.setError(null);
        etYears.setError(null);
        etBio.setError(null);
    }

    private void setErr(EditText et, String msg) {
        et.setError(msg);
        if (et.hasFocus()) return;
        et.requestFocus();
    }

    private String t(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /** 电话校验：允许 +、空格、连字符、括号；数字总数 10~15 位 */
    private boolean isValidPhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        return digits.length() >= 10 && digits.length() <= 15
                && phone.matches("^\\+?[0-9 ()-]{7,20}$");
    }

    private void setLoading(boolean b) {
        if (progress != null) progress.setVisibility(b ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!b);
        etEmail.setEnabled(!b);
        etPassword.setEnabled(!b);
        etConfirmPassword.setEnabled(!b);
        etFirstName.setEnabled(!b);
        etLastName.setEnabled(!b);
        etPhone.setEnabled(!b);
        etSubjects.setEnabled(!b);
        etYears.setEnabled(!b);
        etBio.setEnabled(!b);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
