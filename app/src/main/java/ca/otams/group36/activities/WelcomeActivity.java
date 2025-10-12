/**
 * OTAMS Project
 * Author: Tianqi Jiang
 * University of Ottawa
 *
 * Description:
 * Displays a personalized welcome page after login, styled with uOttawa colors.
 */

package ca.otams.group36.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ca.otams.group36.R;

public class WelcomeActivity extends AppCompatActivity {

    private TextView textWelcome, textRole, textSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("OTAMS");
        }

        textWelcome = findViewById(R.id.textWelcome);
        textRole = findViewById(R.id.textRole);
        textSubtitle = findViewById(R.id.textSubtitle);

        String name = getIntent().getStringExtra("name");
        String role = getIntent().getStringExtra("role");

        textWelcome.setText("Welcome, " + name + "!");
        textRole.setText("Role: " + role);
        textSubtitle.setText("University of Ottawa");
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}