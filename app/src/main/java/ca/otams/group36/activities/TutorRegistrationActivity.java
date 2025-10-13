/**
 * OTAMS Project
 * Author: Yufeng Zhong
 * University of Ottawa

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

import java.util.List;

import ca.otams.group36.R;
import ca.otams.group36.auth.RegistrationRepository;
import ca.otams.group36.auth.Validation;

public class TutorRegistrationActivity extends AppCompatActivity {

    // TextInputLayouts（用于显示内联错误）
    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword,
            tilFirstName, tilLastName, tilPhone, tilSubjects, tilYears, tilBio;

    // 输入框（对应 XML 的 TextInputEditText）
    private TextInputEditText etEmail, etPassword, etConfirmPassword,
            etFirstName, etLastName, etPhone, etSubjects, etYears, etBio;

    private Button btnRegister;
    private ProgressBar progress;

    // 数据库函数层（Firebase Auth + Firestore）
    private RegistrationRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_registration);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setTitle(getString(R.string.register_title_tutor));
        }

        // 绑定视图（ID 必须与 XML 一致）
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilPhone = findViewById(R.id.tilPhone);
        tilSubjects = findViewById(R.id.tilSubjects);
        tilYears = findViewById(R.id.tilYears);
        tilBio = findViewById(R.id.tilBio);

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
        progress = findViewById(R.id.progress);

        repo = new RegistrationRepository();

        // 输入改动即清除错误
        clearOnChange(tilEmail, etEmail);
        clearOnChange(tilPassword, etPassword);
        clearOnChange(tilConfirmPassword, etConfirmPassword);
        clearOnChange(tilFirstName, etFirstName);
        clearOnChange(tilLastName, etLastName);
        clearOnChange(tilPhone, etPhone);
        clearOnChange(tilSubjects, etSubjects);
        clearOnChange(tilYears, etYears);
        clearOnChange(tilBio, etBio);

        btnRegister.setOnClickListener(v -> submit());
    }

    private void submit() {
        resetErrors();

        String email = t(etEmail);
        String pwd = t(etPassword);
        String confirm = t(etConfirmPassword);
        String first = t(etFirstName);
        String last = t(etLastName);
        String phone = t(etPhone);
        String subjectsRaw = t(etSubjects);
        String yearsRaw = t(etYears);
        String bio = t(etBio);

        boolean ok = true;

        // 基础必填
        if (!Validation.nonEmpty(first)) { tilFirstName.setError(getString(R.string.err_required)); ok = false; }
        if (!Validation.nonEmpty(last))  { tilLastName.setError(getString(R.string.err_required)); ok = false; }
        if (!Validation.nonEmpty(subjectsRaw)) { tilSubjects.setError(getString(R.string.err_required)); ok = false; }

        // 邮箱/密码校验
        if (!Validation.email(email)) { tilEmail.setError(getString(R.string.err_invalid_email)); ok = false; }
        if (!Validation.password(pwd)) { tilPassword.setError(getString(R.string.err_password_short)); ok = false; }
        if (!pwd.equals(confirm)) { tilConfirmPassword.setError(getString(R.string.err_password_mismatch)); ok = false; }

        // 电话可选但格式校验
        if (!Validation.phone(phone)) { tilPhone.setError(getString(R.string.err_invalid_phone)); ok = false; }

        // years 非负整数
        Integer years = Validation.parseNonNegativeInt(yearsRaw);
        if (years == null) { tilYears.setError(getString(R.string.err_nonnegative_number)); ok = false; }

        if (!ok) return;

        // 解析科目（用工具函数）
        List<String> subjects = Validation.parseSubjects(subjectsRaw);

        setLoading(true);

        // 空串转为 null，避免写入空值
        final String phoneOrNull = phone.isEmpty() ? null : phone;
        final String bioOrNull = bio.isEmpty() ? null : bio;

        // 调用数据库函数层
        repo.registerTutor(
                email, pwd, first, last, phoneOrNull, subjects, years, bioOrNull,
                new RegistrationRepository.RegistrationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(TutorRegistrationActivity.this,
                                "Tutor registered successfully! Pending approval.", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(TutorRegistrationActivity.this, message, Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                }
        );
    }

    /* ---------- 工具方法 ---------- */

    private void resetErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilPhone.setError(null);
        tilSubjects.setError(null);
        tilYears.setError(null);
        tilBio.setError(null);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);

        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
        etFirstName.setEnabled(!loading);
        etLastName.setEnabled(!loading);
        etPhone.setEnabled(!loading);
        etSubjects.setEnabled(!loading);
        etYears.setEnabled(!loading);
        etBio.setEnabled(!loading);
    }

    private static String t(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static void clearOnChange(final TextInputLayout til, TextInputEditText et) {
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { til.setError(null); }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
