/**
 * OTAMS Project
 * Author: Lige Xiao & Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Displays a personalized welcome page after login, styled with uOttawa colors.
 */

package ca.otams.group36.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ca.otams.group36.MainActivity;
import ca.otams.group36.R;

public class WelcomeActivity extends AppCompatActivity {

    private TextView textWelcome, textRole, textSubtitle;
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("OTAMS");
        }

        // Match layout IDs
        textWelcome = findViewById(R.id.textWelcome);
        textRole = findViewById(R.id.textRole);
        textSubtitle = findViewById(R.id.textSubtitle);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Retrieve extras safely
        String name = getIntent().getStringExtra("name");
        String role = getIntent().getStringExtra("role");

        // Null safety (fallback values)
        if (name == null) name = "User";
        if (role == null) role = "Student";

        // Set text content
        textWelcome.setText("Welcome, " + name + "!");
        textRole.setText("Role: " + role);
        textSubtitle.setText("University of Ottawa");

        // Logout button → return to main
        buttonLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}