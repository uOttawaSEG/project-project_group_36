/**
 * OTAMS Project
 * Author: Lige Xiao
 * University of Ottawa
 *
 * Description:
 * Displays a personalized welcome page after login, styled with uOttawa colors.
 */

package ca.otams.group36.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.Button;

import ca.otams.group36.MainActivity;
import ca.otams.group36.R;

public class WelcomeActivity extends AppCompatActivity {

    private TextView textWelcome, textRole, textSubtitle;
    private Button buttonLogout;

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
        buttonLogout = findViewById(R.id.buttonLogout);

        String name = getIntent().getStringExtra("name");
        String role = getIntent().getStringExtra("role");

        textWelcome.setText("Welcome, " + name + "!");
        textRole.setText("Role: " + role);
        textSubtitle.setText("University of Ottawa");

        buttonLogout.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class))
        );

    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}