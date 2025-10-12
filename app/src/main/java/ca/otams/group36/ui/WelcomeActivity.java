package ca.otams.group36.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ca.otams.group36.R;
import ca.otams.group36.auth.*;

public class WelcomeActivity extends AppCompatActivity {
    private SessionManager session;
    private AuthRepository repo;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome); // your XML

        // Attach toolbar (we will put a Logout menu here)
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.title_welcome);

        session = new SessionManager(this);
        repo = new FakeAuthRepository();

        // Resolve role from Intent or session
        String role = getIntent().getStringExtra("role");
        if (role == null && session.load() != null) role = session.load().name();
        if (role == null) role = "UNKNOWN";

        // Bind to your IDs in the layout
        TextView textWelcome = findViewById(R.id.textWelcome);
        TextView textRole    = findViewById(R.id.textRole);

        // Fill texts (IDs come from your XML)
        textWelcome.setText(R.string.welcome_message);
        textRole.setText(getString(R.string.role_label, role));
    }

    // Inflate a simple Logout menu item in the toolbar
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            session.clear();
            repo.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
