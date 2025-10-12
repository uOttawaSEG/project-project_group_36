package ca.otams.group36.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ca.otams.group36.R;
import ca.otams.group36.auth.*;

public class LoginActivity extends AppCompatActivity {
    private EditText editEmail, editPassword;
    private Button buttonLogin;
    private SessionManager session;
    private AuthRepository repo;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // your XML

        // Setup toolbar from layout (optional title)
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.title_login);

        // Wire up existing views by ID from your layout
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Minimal auth plumbing
        session = new SessionManager(this);
        repo = new FakeAuthRepository();

        buttonLogin.setOnClickListener(v -> doLogin());
    }

    @Override protected void onStart() {
        super.onStart();
        // Auto-login if a previous role is stored
        Role cached = session.load();
        if (cached != null) {
            goWelcome(cached);
            finish();
        }
    }

    private void doLogin() {
        String email = editEmail.getText().toString().trim();
        String pwd   = editPassword.getText().toString();

        try {
            Role role = repo.login(email, pwd);
            session.save(role);
            goWelcome(role);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage() == null ? "Login failed" : e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void goWelcome(Role role) {
        Intent it = new Intent(this, WelcomeActivity.class);
        it.putExtra("role", role.name());
        startActivity(it);
    }
}
