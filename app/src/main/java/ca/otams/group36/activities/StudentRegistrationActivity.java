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

import ca.otams.group36.R;
import ca.otams.group36.auth.RegistrationRepository;
import ca.otams.group36.auth.Validation;

public class StudentRegistrationActivity extends AppCompatActivity {

    // TextInputLayouts 用于内联错误提示
    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword,
            tilFirstName, tilLastName, tilPhone, tilProgram;

    // 输入框（与 XML 的 TextInputEditText 匹配）
    private TextInputEditText etEmail, etPassword, etConfirmPassword,
            etFirstName, etLastName, etPhone, etProgram;

    private Button btnRegister;
    private ProgressBar progress;

    private RegistrationRepository repo;

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
            getSupportActionBar().setTitle(getString(R.string.register_title));
        }

        // 绑定 View —— ID 必须与 XML 一致
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilPhone = findViewById(R.id.tilPhone);
        tilProgram = findViewById(R.id.tilProgram);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etProgram = findViewById(R.id.etProgram);

        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);

        // 仓库（数据库函数层：Firebase Auth + Firestore）
        repo = new RegistrationRepository();

        // 输入改动时清除错误
        clearOnChange(tilEmail, etEmail);
        clearOnChange(tilPassword, etPassword);
        clearOnChange(tilConfirmPassword, etConfirmPassword);
        clearOnChange(tilFirstName, etFirstName);
        clearOnChange(tilLastName, etLastName);
        clearOnChange(tilPhone, etPhone);
        clearOnChange(tilProgram, etProgram);

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
        String program = t(etProgram);

        boolean ok = true;

        // 必填
        if (!Validation.nonEmpty(first)) { tilFirstName.setError(getString(R.string.err_required)); ok = false; }
        if (!Validation.nonEmpty(last))  { tilLastName.setError(getString(R.string.err_required));  ok = false; }
        if (!Validation.nonEmpty(program)) { tilProgram.setError(getString(R.string.err_required)); ok = false; }

        // 邮箱格式
        if (!Validation.email(email)) { tilEmail.setError(getString(R.string.err_invalid_email)); ok = false; }

        // 密码长度 & 确认一致
        if (!Validation.password(pwd)) { tilPassword.setError(getString(R.string.err_password_short)); ok = false; }
        if (!pwd.equals(confirm)) { tilConfirmPassword.setError(getString(R.string.err_password_mismatch)); ok = false; }

        // 电话可选但校验格式
        if (!Validation.phone(phone)) { tilPhone.setError(getString(R.string.err_invalid_phone)); ok = false; }

        if (!ok) return;

        // 连接“注册数据 → 数据库函数”
        setLoading(true);

        // 空字符串转为 null，避免把空值写进 Firestore
        final String phoneOrNull = phone.isEmpty() ? null : phone;

        repo.registerStudent(
                email, pwd, first, last, phoneOrNull, program,
                new RegistrationRepository.RegistrationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(StudentRegistrationActivity.this, getString(R.string.toast_created), Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(StudentRegistrationActivity.this, message, Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                }
        );
    }

    // 工具方法 —— 清空所有错误
    private void resetErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilPhone.setError(null);
        tilProgram.setError(null);
    }

    // 显示/隐藏加载，并禁用输入与按钮
    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);

        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
        etFirstName.setEnabled(!loading);
        etLastName.setEnabled(!loading);
        etPhone.setEnabled(!loading);
        etProgram.setEnabled(!loading);
    }

    // 取安全文本
    private static String t(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    // 文本变化后清除错误
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
